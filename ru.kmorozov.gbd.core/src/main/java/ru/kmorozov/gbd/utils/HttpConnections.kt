package ru.kmorozov.gbd.utils

import com.google.api.client.http.HttpHeaders
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.TorProxy
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.*
import java.net.Proxy.NO_PROXY
import java.net.Proxy.Type
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * Created by km on 22.11.2015.
 */
class HttpConnections private constructor() {

    companion object {

        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.3112.107 Mobile Safari/537.36"
        private val baseUrls = ConcurrentHashMap<UrlType, URI>(1)

        private val logger = Logger.getLogger(GBDOptions.debugEnabled, HttpConnections::class.java)

        fun getResponse(host: InetSocketAddress, urlType: UrlType): Optional<HttpResponse<*>> {
            val baseUrl = getBaseUrl(urlType)
            val proxy: Proxy
            if ("localhost" == host.hostName && 1 == host.port)
                proxy = NO_PROXY
            else if (TorProxy.TOR_HOST == host.hostName && 9150 == host.port)
                proxy = Proxy(Type.SOCKS, InetSocketAddress(TorProxy.TOR_HOST, TorProxy.TOR_HTTP_PORT))
            else
                proxy = Proxy(Type.HTTP, host)

            try {
                val client = if (proxy == NO_PROXY) HttpClient.newBuilder().build() else HttpClient.newBuilder()
                    .proxy(ProxySelector.of(host)).build()
                val reqBuilder = HttpRequest.newBuilder()
                    .uri(baseUrl).GET().timeout(Duration.ofMillis(10000)).setHeader("User-Agent", USER_AGENT)

                return Optional.of(client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream()))
            } catch (e: IOException) {
                if (GBDOptions.debugEnabled)
                    logger.info("Proxy $proxy error: " + e.message)

                return Optional.empty()
            } catch (ste: SocketTimeoutException) {
                if (GBDOptions.debugEnabled) {
                    logger.info("Proxy $proxy timeout")
                }

                return Optional.empty()
            } catch (ce: ConnectException) {
                if (GBDOptions.debugEnabled) {
                    logger.info("Cannot connect $proxy")
                }

                return Optional.empty()
            }
        }

        fun getHeaders(proxy: HttpHostExt): HttpHeaders {
            val headers = HttpHeaders()
            headers.userAgent = USER_AGENT
            headers.cookie = proxy.cookie
            return headers
        }

        fun getCookieString(proxy: InetSocketAddress, urlType: UrlType): String {
            val resp = getResponse(proxy, urlType)
            if (resp.isEmpty)
                return ""
            else {
                if (GBDOptions.debugEnabled)
                    logger.info("Got cookies for $proxy : ${resp.get().headers()}")

                return resp.get().headers().allValues("set-cookie").stream()
                    .map { s -> s.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                    .collect(Collectors.joining(";"))
            }
        }

        private fun getBaseUrl(urlType: UrlType): URI {
            return baseUrls.computeIfAbsent(urlType) {
                when (urlType) {
                    UrlType.GOOGLE_BOOKS -> URI.create("https://www.google.com/")
                    UrlType.GOOGLE_BOOK_INFO -> URI.create("https://www.google.com/")
                    UrlType.JSTOR -> URI.create("https://www.jstor.org")
                    else -> URI.create("http://www.ya.ru")
                }
            }
        }
    }
}
