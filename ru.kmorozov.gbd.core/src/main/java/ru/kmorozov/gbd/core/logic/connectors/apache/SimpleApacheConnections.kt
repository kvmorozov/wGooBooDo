package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.client.CookieStore
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.utils.HttpConnections
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by km on 22.05.2016.
 */
open class SimpleApacheConnections : IApacheConnectionFactory {
    protected val builder: HttpClientBuilder
    private val builderWithTimeout: HttpClientBuilder
    private val clientsMap = ConcurrentHashMap<HttpHost, CloseableHttpClient>()
    private val withTimeoutClientsMap: MutableMap<HttpHost, CloseableHttpClient> = ConcurrentHashMap<HttpHost, CloseableHttpClient>()
    protected val cookieStoreMap: MutableMap<HttpHostExt, CookieStore> = ConcurrentHashMap<HttpHostExt, CookieStore>()
    private val noProxyClient: CloseableHttpClient
    private val noProxyClientWithTimeout: CloseableHttpClient

    init {
        val connPool = PoolingHttpClientConnectionManager()
        connPool.maxTotal = 200
        connPool.defaultMaxPerRoute = 200

        builder = HttpClientBuilder.create()
                .setUserAgent(HttpConnections.USER_AGENT)
                .setConnectionManager(connPool)
                .setConnectionManagerShared(true)
                .disableRedirectHandling()

        builderWithTimeout = HttpClientBuilder.create()
                .setUserAgent(HttpConnections.USER_AGENT)
                .setConnectionManager(connPool)

        var defaultCookieStore: CookieStore = BasicCookieStore()
        try {
            defaultCookieStore = getCookieStore()!!
        } catch (ex: Exception) {
        }

        val defaultHeaders = getHeaders()

        noProxyClient = builder.setDefaultCookieStore(defaultCookieStore).setDefaultHeaders(defaultHeaders).build()
        noProxyClientWithTimeout = builderWithTimeout.setDefaultCookieStore(defaultCookieStore).setDefaultHeaders(defaultHeaders).build()

        val requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(HttpConnector.CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(HttpConnector.CONNECT_TIMEOUT).build()

        builderWithTimeout.setDefaultRequestConfig(requestConfig)
    }

    override fun getClient(proxy: HttpHostExt, withTimeout: Boolean): HttpClient {
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

    protected open fun getCookieStore(proxy: HttpHostExt = AbstractProxyListProvider.INSTANCE.getSomeProxy()): CookieStore? {
        return null
    }

    protected open fun getHeaders(proxy: HttpHostExt = AbstractProxyListProvider.INSTANCE.getSomeProxy()): List<Header>? {
        return null
    }

    override fun closeAllConnections() {
        try {
            noProxyClient.close()
            noProxyClientWithTimeout.close()

            for (client in clientsMap.values)
                (client as Closeable).close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {

        internal val INSTANCE = SimpleApacheConnections()
    }
}
