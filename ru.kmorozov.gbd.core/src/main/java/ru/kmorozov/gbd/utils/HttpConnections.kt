package ru.kmorozov.gbd.utils

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.javanet.NetHttpTransport.Builder
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.TorProxy
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.NO_PROXY
import java.net.Proxy.Type
import java.net.SocketTimeoutException
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
        private val headers = HttpHeaders().setUserAgent(USER_AGENT)
        private val baseUrls = ConcurrentHashMap<UrlType, GenericUrl>(1)

        private val logger = Logger.getLogger(GBDOptions.debugEnabled, HttpConnections::class.java)

        fun getResponse(host: InetSocketAddress, urlType: UrlType): Optional<HttpResponse> {
            val baseUrl = getBaseUrl(urlType)
            val proxy: Proxy
            if ("localhost" == host.hostName && 1 == host.port)
                proxy = NO_PROXY
            else if (TorProxy.TOR_HOST == host.hostName && 9150 == host.port)
                proxy = Proxy(Type.SOCKS, InetSocketAddress(TorProxy.TOR_HOST, TorProxy.TOR_HTTP_PORT))
            else
                proxy = Proxy(Type.HTTP, host)

            try {
                val request = if (proxy == NO_PROXY) Builder().build() else Builder().setProxy(proxy).build()
                val responce =
                    request.createRequestFactory().buildGetRequest(baseUrl).setHeaders(headers).setConnectTimeout(10000)
                        .execute()
                return Optional.of(responce)
            } catch (e: IOException) {
                if (GBDOptions.debugEnabled)
                    logger.info("Proxy $proxy error:" + e.localizedMessage)

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
            else
                return (resp.get().headers["set-cookie"] as Collection<String>).stream()
                    .map { s -> s.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }
                    .collect(Collectors.joining(";"))
        }

        private fun getBaseUrl(urlType: UrlType): GenericUrl {
            return baseUrls.computeIfAbsent(urlType) {
                when (urlType) {
                    UrlType.GOOGLE_BOOKS -> GenericUrl("https://www.google.com/")
                    UrlType.GOOGLE_BOOK_INFO -> GenericUrl("https://www.google.com/")
                    UrlType.JSTOR -> GenericUrl("https://www.jstor.org")
                    else -> GenericUrl("http://www.ya.ru")
                }
            }
        }
    }
}
