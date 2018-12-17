package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.cookie.CookieStore
import org.apache.hc.client5.http.cookie.SetCookie
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.UrlType
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.utils.HttpConnections

import javax.net.ssl.SSLContext
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Created by km on 22.05.2016.
 */
class ApacheConnections private constructor() {
    private val builder: HttpClientBuilder
    private val builderWithTimeout: HttpClientBuilder
    private val clientsMap = ConcurrentHashMap<HttpHost, HttpClient>()
    private val withTimeoutClientsMap = ConcurrentHashMap<HttpHost, HttpClient>()
    private val cookieStoreMap = ConcurrentHashMap<HttpHostExt, CookieStore>()
    private var noProxyClient: CloseableHttpClient? = null

    init {
        val connPool = PoolingHttpClientConnectionManager()
        connPool.maxTotal = 200
        connPool.defaultMaxPerRoute = 200

        builder = HttpClientBuilder.create().setUserAgent(HttpConnections.USER_AGENT).setConnectionManager(connPool).setConnectionManagerShared(true)

        builderWithTimeout = HttpClientBuilder.create().setUserAgent(HttpConnections.USER_AGENT).setConnectionManager(connPool)

        try {
            val sslContext = SSLContextBuilder().loadTrustMaterial(null) { _, _ -> true }.build()
            //            builder.setSSLContext(sslContext);
            //            builderWithTimeout.setSSLContext(sslContext);
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(HttpConnector.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(HttpConnector.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS).build()

        builderWithTimeout.setDefaultRequestConfig(requestConfig)
    }

    fun getClient(proxy: HttpHostExt, withTimeout: Boolean): HttpClient? {
        val _builder = if (withTimeout) builderWithTimeout else builder
        _builder.setDefaultCookieStore(getCookieStore(proxy))

        if (proxy.isLocal) {
            if (null == noProxyClient) noProxyClient = _builder.build()

            return noProxyClient
        } else {
            val _map = if (withTimeout) withTimeoutClientsMap else clientsMap

            val host = HttpHost(proxy.host.address)

            return (_map as java.util.Map<HttpHost, HttpClient>).computeIfAbsent(host) { _builder.setProxy(host).build() }
        }
    }

    fun closeAllConnections() {
        try {
            if (null != noProxyClient) noProxyClient!!.close()

            if (null != clientsMap)
                for (client in clientsMap.values)
                    (client as Closeable).close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getCookieStore(proxy: HttpHostExt): CookieStore {
        var cookieStore: CookieStore = cookieStoreMap[proxy] ?: throw RuntimeException()

        if (null == cookieStore) {
            synchronized(proxy) {
                cookieStore = BasicCookieStore()
                val cookies = proxy.getHeaders(UrlType.GOOGLE_BOOKS).cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                for (cookieEntry in cookies) {
                    val cookieParts = cookieEntry.split("=".toRegex(), 2).toTypedArray()

                    if (1 < cookieParts.size && LibraryFactory.needSetCookies()) {
                        val cookie = BasicClientCookie(cookieParts[0], cookieParts[1])
                        cookie.domain = ".google.ru"
                        cookie.path = "/"
                        cookieStore.addCookie(cookie)
                    }
                }

                cookieStoreMap.put(proxy, cookieStore as BasicCookieStore)
            }
        }

        return cookieStore
    }

    companion object {

        internal val INSTANCE = ApacheConnections()
    }
}
