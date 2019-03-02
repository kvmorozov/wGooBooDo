package ru.kmorozov.gbd.core.logic.connectors.http2native

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
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
                        .uri(uri).GET().timeout(Duration.ofMillis((if (withTimeout) HttpConnector.CONNECT_TIMEOUT else HttpConnector.CONNECT_TIMEOUT * 10).toLong()))

                if (needHeaders(rqUrl)) {
                    val headers = proxy.getHeaders(getUrlType(rqUrl))
                    reqBuilder.setHeader("User-Agent", headers.userAgent)
                    reqBuilder.setHeader("Cookie", headers.cookie)
                }

                resp = getContent(reqBuilder.build(), proxy, 0)
            } else
                throw RuntimeException("Invalid proxy config!")

            if (null == resp)
                logger.finest("No response at url $rqUrl with proxy $proxy")

            return Http2Response(resp!!)
        } catch (ioe: IOException) {
            logger.severe("Connection error: ${ioe.message}")
            throw Http2ResponseException(ioe)
        }

    }

    @Throws(IOException::class)
    private fun getContent(req: HttpRequest, proxy: HttpHostExt, attempt: Int): HttpResponse<*>? {
        var attempt = attempt
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure()
            return null
        }

        if (1 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.uri().toString()))
                Thread.sleep((HttpConnector.SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return DEFAULT_CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray())
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(req, proxy, ++attempt)
        } catch (ste1: InterruptedException) {
            proxy.registerFailure()
            return getContent(req, proxy, ++attempt)
        }

    }

    @Throws(Exception::class)
    override fun close() {

    }

    companion object {

        private val httpClientsMap = ConcurrentHashMap<String, HttpClient>()
        private val logger = Logger.getLogger(HttpConnector::class.java)
        private val DEFAULT_CLIENT = HttpClient.newHttpClient()
    }
}
