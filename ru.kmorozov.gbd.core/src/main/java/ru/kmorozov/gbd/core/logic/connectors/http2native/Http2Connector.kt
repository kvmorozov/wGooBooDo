package ru.kmorozov.gbd.core.logic.connectors.http2native

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.ProxySelector
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class Http2Connector : HttpConnector() {

    private fun getClient(proxy: HttpHostExt): HttpClient {
        return httpClientsMap.computeIfAbsent(getProxyKey(proxy)) { HttpClient.newBuilder().proxy(ProxySelector.of(proxy.host)).build() }
    }

    @Throws(IOException::class)
    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        try {
            val uri = URI.create(rqUrl)

            if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable) return EMPTY_RESPONSE

            val resp: HttpResponse<*>?
            if (validateProxy(rqUrl, proxy)) {
                val reqBuilder = HttpRequest.newBuilder()
                        .uri(uri).GET().timeout(Duration.ofMillis((if (withTimeout) CONNECT_TIMEOUT else CONNECT_TIMEOUT * 10).toLong()))

                if (needHeaders(rqUrl)) {
                    val headers = proxy.getHeaders(getUrlType(rqUrl))
                    reqBuilder.setHeader("User-Agent", headers.userAgent)
                    reqBuilder.setHeader("Cookie", headers.cookie)
                }

                resp = getContent(reqBuilder.build(), proxy, 0)
            } else {
                logger.error("Invalid proxy config! $proxy")
                return EMPTY_RESPONSE
            }

            if (null == resp)
                logger.finest("No response at url $rqUrl with proxy $proxy")

            if (resp?.statusCode() == 403) {
                proxy.reset()
                return EMPTY_RESPONSE
            } else
                return Http2Response(resp!!)
        } catch (ioe: IOException) {
            logger.severe("Connection error: ${ioe.message}")

            if (GBDOptions.debugEnabled)
                logger.info(ioe.stackTraceToString())

            return EMPTY_RESPONSE
        }

    }

    @Throws(IOException::class)
    private fun getContent(req: HttpRequest, proxy: HttpHostExt, attempt: Int): HttpResponse<*>? {
        if (MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure()
            return null
        }

        if (1 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.uri().toString()))
                Thread.sleep((SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return DEFAULT_CLIENT.send(req, HttpResponse.BodyHandlers.ofInputStream())
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(req, proxy, attempt + 1)
        } catch (ste1: InterruptedException) {
            proxy.registerFailure()
            return getContent(req, proxy, attempt + 1)
        }

    }

    @Throws(Exception::class)
    override fun close() {

    }

    companion object {

        private val httpClientsMap = ConcurrentHashMap<String, HttpClient>()
        private val logger = Logger.getLogger(GBDOptions.debugEnabled, Http2Connector::class.java)
        private val DEFAULT_CLIENT = HttpClient.newHttpClient()
    }
}
