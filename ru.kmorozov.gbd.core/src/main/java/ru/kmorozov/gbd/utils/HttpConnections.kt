package ru.kmorozov.gbd.utils

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.javanet.NetHttpTransport.Builder
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.NO_PROXY
import java.net.Proxy.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * Created by km on 22.11.2015.
 */
class HttpConnections private constructor() {

    private val headersMap = ConcurrentHashMap<HttpHostExt, HttpHeaders>()

    private fun _getHeaders(proxy: HttpHostExt): HttpHeaders {
        return (headersMap as MutableMap<HttpHostExt, HttpHeaders>).computeIfAbsent(proxy) { httpHostExt ->
            val _headers = HttpHeaders()
            _headers.userAgent = USER_AGENT
            _headers.cookie = httpHostExt.cookie

            _headers
        }
    }

    companion object {

        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.3112.107 Mobile Safari/537.36"
        private val headers = HttpHeaders().setUserAgent(USER_AGENT)
        private val INSTANCE = HttpConnections()
        private val baseUrls = HashMap<UrlType, GenericUrl>(1)

        fun getResponse(host: InetSocketAddress, urlType: UrlType): HttpResponse? {
            val baseUrl = getBaseUrl(urlType)
            val proxy: Proxy
            if ("localhost" == host.hostName && 1 == host.port)
                proxy = NO_PROXY
            else if ("localhost" == host.hostName && 9150 == host.port)
                proxy = Proxy(Type.SOCKS, InetSocketAddress("localhost", 9150))
            else
                proxy = Proxy(Type.HTTP, host)

            try {
                val request = if (proxy == NO_PROXY) Builder().build() else Builder().setProxy(proxy).build()
                return request.createRequestFactory().buildGetRequest(baseUrl).setHeaders(headers).setConnectTimeout(10000).execute()
            } catch (e: IOException) {
                return null
            }
        }

        fun getHeaders(proxy: HttpHostExt): HttpHeaders {
            return INSTANCE._getHeaders(proxy)
        }

        fun getCookieString(proxy: InetSocketAddress, urlType: UrlType): String {
            try {
                val resp = getResponse(proxy, urlType) ?: return "ppp"

                return (resp.headers["set-cookie"] as Collection<String>).stream()
                        .map { s -> s.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] }.collect(Collectors.joining(";"))
            } catch (e: Exception) {
                return ""
            }

        }

        private fun _setDefaultCookies(cookiesMap: Map<String, String>) {
            val cookieBuilder = StringBuilder()

            for (cookieEntry in cookiesMap) {
                cookieBuilder.append(cookieEntry.key).append('=').append(cookieEntry.value).append("; ")
            }

            HttpHostExt.NO_PROXY.cookie = cookieBuilder.toString()
        }

        private fun getBaseUrl(urlType: UrlType): GenericUrl {
            return baseUrls.computeIfAbsent(urlType) {
                when (urlType) {
                    UrlType.GOOGLE_BOOKS -> {
                        val anyContext = ExecutionContext.INSTANCE.getContexts(false).stream().filter { bookContext -> bookContext.bookInfo.bookData is GoogleBookData }.findAny()
                        if (!anyContext.isPresent)
                            GenericUrl("https://books.google.ru")
                        else {
                            GenericUrl((anyContext.get().bookInfo.bookData as GoogleBookData).baseUrl)
                        }
                    }
                    UrlType.JSTOR -> GenericUrl("https://www.jstor.org")
                    else -> GenericUrl("http://www.ya.ru")
                }
            }
        }
    }
}
