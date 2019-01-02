package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.cookie.CookieStore
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt.Companion.NO_PROXY
import ru.kmorozov.gbd.core.logic.Proxy.UrlType
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.utils.HttpConnections
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
    private val clientsMap = ConcurrentHashMap<HttpHost, CloseableHttpClient>()
    private val withTimeoutClientsMap: MutableMap<HttpHost, CloseableHttpClient> = ConcurrentHashMap<HttpHost, CloseableHttpClient>()
    private val cookieStoreMap: MutableMap<HttpHostExt, CookieStore> = ConcurrentHashMap<HttpHostExt, CookieStore>()
    private val noProxyClient: CloseableHttpClient
    private val noProxyClientWithTimeout: CloseableHttpClient
    private val defaultCookieStore: CookieStore

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

        defaultCookieStore = getCookieStore(NO_PROXY)

        noProxyClient = builder.setDefaultCookieStore(defaultCookieStore).build()
        noProxyClientWithTimeout = builderWithTimeout.setDefaultCookieStore(defaultCookieStore).build()

        val requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(HttpConnector.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(HttpConnector.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS).build()

        builderWithTimeout.setDefaultRequestConfig(requestConfig)
    }

    fun getClient(proxy: HttpHostExt, withTimeout: Boolean): HttpClient {
        if (proxy.isLocal) {
            return if (withTimeout) noProxyClientWithTimeout else noProxyClient
        } else {
            val _map = if (withTimeout) withTimeoutClientsMap else clientsMap

            val _builder = if (withTimeout) builderWithTimeout else builder
            _builder.setDefaultCookieStore(getCookieStore(proxy))

            val host = HttpHost(proxy.host.address)

            return _map.computeIfAbsent(host) { _builder.setProxy(host).build() }
        }
    }

    fun closeAllConnections() {
        try {
            noProxyClient.close()
            noProxyClientWithTimeout.close()

            for (client in clientsMap.values)
                (client as Closeable).close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getCookieStore(proxy: HttpHostExt): CookieStore {
        return cookieStoreMap.computeIfAbsent(proxy) {
            val cookieStore = BasicCookieStore()
            val cookies = proxy.getHeaders(UrlType.GOOGLE_BOOKS).cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (cookieEntry in cookies) {
                val cookieParts = cookieEntry.split("=".toRegex(), 2).toTypedArray()

                if (1 < cookieParts.size && ExecutionContext.INSTANCE.defaultMetadata.needSetCookies()) {
                    val cookie = BasicClientCookie(cookieParts[0], cookieParts[1])
                    cookie.domain = ".google.ru"
                    cookie.path = "/"
                    cookieStore.addCookie(cookie)
                }
            }
            cookieStore
        }
    }

    companion object {

        internal val INSTANCE = ApacheConnections()
    }
}
