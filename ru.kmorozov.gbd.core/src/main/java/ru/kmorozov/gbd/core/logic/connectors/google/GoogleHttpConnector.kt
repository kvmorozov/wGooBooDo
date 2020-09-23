package ru.kmorozov.gbd.core.logic.connectors.google

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.javanet.NetHttpTransport.Builder
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.logger.Logger
import java.net.SocketTimeoutException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by km on 17.05.2016.
 */
class GoogleHttpConnector : HttpConnector() {

    private fun getFactory(proxy: HttpHostExt): HttpRequestFactory {
        val key = getProxyKey(proxy)

        return httpFactoryMap.getOrPut(key, { Builder().setProxy(proxy.proxy).build().createRequestFactory() })
    }

    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        try {
            val url = GenericUrl(URI.create(rqUrl))

            if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable) return EMPTY_RESPONSE

            val resp: Response
            if (validateProxy(rqUrl, proxy)) {
                val req = getFactory(proxy).buildGetRequest(url)
                        .setConnectTimeout(if (withTimeout) CONNECT_TIMEOUT else CONNECT_TIMEOUT * 10)
                        .setSuppressUserAgentSuffix(true)
                if (needHeaders(rqUrl))
                    req.headers = proxy.getHeaders(getUrlType(rqUrl))

                resp = getContent(req, proxy, 0)
            } else {
                logger.error("Invalid proxy config! " + proxy.toString())
                return EMPTY_RESPONSE
            }

            if (resp.empty)
                logger.finest("No response at url $rqUrl with proxy $proxy")

            return resp
        } catch (hre: HttpResponseException) {
            if (hre.statusCode != 404)
                logger.severe("Connection error: ${hre.statusMessage}")
            return EMPTY_RESPONSE
        }

    }

    private fun getContent(req: HttpRequest, proxy: HttpHostExt, attempt: Int): Response {
        if (MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure()
            return EMPTY_RESPONSE
        }

        if (1 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.url.toString()))
                Thread.sleep((SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            val res = GoogleResponse(req.execute())
            return res;
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(req, proxy, attempt + 1)
        } catch (hre: HttpResponseException) {
            proxy.registerFailure()
            if (hre.statusCode == 403) {
                proxy.reset()
                req.headers = proxy.getHeaders(UrlType.GOOGLE_BOOKS)
                return getContent(req, proxy, attempt + 1)
            } else if (hre.statusCode == 404)
                return EMPTY_RESPONSE
            else
                throw (hre)
        }
    }

    override fun close() {

    }

    companion object {

        private val logger = Logger.getLogger(HttpConnector::class.java)
        private val httpFactoryMap = ConcurrentHashMap<String, HttpRequestFactory>()
    }
}
