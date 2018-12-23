package ru.kmorozov.gbd.core.logic.connectors.google

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.javanet.NetHttpTransport.Builder
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONCE
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by km on 17.05.2016.
 */
class GoogleHttpConnector : HttpConnector() {

    private fun getFactory(proxy: HttpHostExt): HttpRequestFactory {
        val key = getProxyKey(proxy)

        return httpFactoryMap.getOrDefault(key, Builder().setProxy(proxy.proxy).build().createRequestFactory())
    }

    @Throws(IOException::class)
    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        try {
            val url = GenericUrl(URI.create(rqUrl))

            if (GBDOptions.secureMode() && proxy.isLocal || !proxy.isAvailable) return EMPTY_RESPONCE

            val resp: Response
            if (validateProxy(rqUrl, proxy)) {
                val req = getFactory(proxy).buildGetRequest(url)
                        .setConnectTimeout(if (withTimeout) HttpConnector.CONNECT_TIMEOUT else HttpConnector.CONNECT_TIMEOUT * 10)
                        .setSuppressUserAgentSuffix(true)
                if (needHeaders(rqUrl))
                    req.headers = proxy.getHeaders(getUrlType(rqUrl))

                resp = getContent(req, proxy, 0)
            } else {
                logger.error("Invalid proxy config! " + proxy.toString())
                return EMPTY_RESPONCE
            }

            if (resp == EMPTY_RESPONCE)
                logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()))

            return resp
        } catch (hre: HttpResponseException) {
            logger.severe("Connection error: " + hre.statusMessage)
            throw GoogleResponseException(hre)
        }

    }

    @Throws(IOException::class)
    private fun getContent(req: HttpRequest, proxy: HttpHostExt, attempt: Int): Response {
        var attempt = attempt
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure()
            return EMPTY_RESPONCE
        }

        if (1 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.url.toString()))
                Thread.sleep((HttpConnector.SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return GoogleResponse(req.execute())
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(req, proxy, ++attempt)
        }

    }

    override fun close() {

    }

    companion object {

        private val logger = Logger.getLogger(HttpConnector::class.java)
        private val httpFactoryMap = ConcurrentHashMap<String, HttpRequestFactory>()
    }
}
