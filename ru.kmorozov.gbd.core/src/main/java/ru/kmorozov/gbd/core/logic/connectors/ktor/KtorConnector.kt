package ru.kmorozov.gbd.core.logic.connectors.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.util.concurrent.ConcurrentHashMap

class KtorConnector : HttpConnector() {

    private fun getClient(rqProxy: HttpHostExt): HttpClient {
        if (rqProxy.isLocal)
            return DEFAULT_CLIENT
        else {
            val urlBuilder = URLBuilder()
            urlBuilder.host = rqProxy.host.hostName
            urlBuilder.port = rqProxy.host.port

            val httpProxy = ProxyBuilder.http(URLBuilder().build())
            return clientsMap.getOrPut(getProxyKey(rqProxy), {
                HttpClient(CIO) {
                    install(HttpTimeout) {
                        requestTimeoutMillis = CONNECT_TIMEOUT.toLong()
                    }
/*                    engine {
                        proxy = httpProxy
                    }*/
                }
            })
        }
    }

    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response = runBlocking<Response> {
        if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable)
            Response.EMPTY_RESPONSE

        val client = getClient(proxy)

        val resp: HttpResponse
        val rqBuilder = HttpRequestBuilder()

        if (validateProxy(rqUrl, proxy)) {
            if (needHeaders(rqUrl)) {
                val headers = proxy.getHeaders(getUrlType(rqUrl))
                rqBuilder.headers.append("User-Agent", headers.userAgent)
                rqBuilder.headers.append("Cookie", headers.cookie)
            }

            rqBuilder.url(rqUrl)

            resp = async { client.get<HttpResponse>(rqBuilder) }.await()
        } else
            throw RuntimeException("Invalid proxy config!")

        KtorResponse(resp)
    }

    override fun close() {
        DEFAULT_CLIENT.close()
    }

    companion object {
        private val DEFAULT_CLIENT = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = CONNECT_TIMEOUT.toLong()
            }
        }
        private val clientsMap = ConcurrentHashMap<String, HttpClient>()
        private val logger = Logger.getLogger(KtorConnector::class.java)
    }
}