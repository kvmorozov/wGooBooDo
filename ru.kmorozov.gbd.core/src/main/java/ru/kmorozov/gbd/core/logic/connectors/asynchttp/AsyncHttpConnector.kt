package ru.kmorozov.gbd.core.logic.connectors.asynchttp

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.util.HashedWheelTimer
import io.netty.util.concurrent.DefaultThreadFactory
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.netty.channel.DefaultChannelPool
import org.asynchttpclient.netty.ssl.DefaultSslEngineFactory
import org.asynchttpclient.proxy.ProxyServer.Builder
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by km on 22.12.2016.
 */
class AsyncHttpConnector : HttpConnector() {

    private val clientsMap = ConcurrentHashMap<String, AsyncHttpClient>()
    private val builder: DefaultAsyncHttpClientConfig.Builder

    private val nioEventLoopGroup: NioEventLoopGroup
    private val pool: DefaultChannelPool
    private val timer: HashedWheelTimer

    private fun getClient(proxy: HttpHostExt): AsyncHttpClient {
        return clientsMap.computeIfAbsent(getProxyKey(proxy), {
            if (!proxy.isLocal)
                builder.setProxyServer(Builder(proxy.host.hostName, proxy.host.port).build())
            DefaultAsyncHttpClient(builder.build())
        })
    }

    override fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        val client = getClient(proxy)
        val builder = client.prepareGet(url)
        for ((key, value) in proxy.getHeaders(getUrlType(url)))
            if ("cookie" != key)
                builder.addHeader(key, value.toString())

        val cookies = proxy.getHeaders(getUrlType(url)).cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cookieEntry in cookies) {
            val cookieParts = cookieEntry.split("=".toRegex(), 2).toTypedArray()
            if (2 != cookieParts.size) continue

            val cookie = DefaultCookie(cookieParts[0], cookieParts[1])
            cookie.setPath("/")
            cookie.setDomain(".google.ru")
            builder.addCookie(cookie)
        }

        try {
            val resp = builder.execute(AsyncHandler(proxy)).get(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            return AsyncHttpResponse(resp)
        } catch (ex: Exception) {
            return EMPTY_RESPONSE
        }
    }

    override fun close() {
        for (client in clientsMap.values)
            try {
                if (!client.isClosed)
                    client.close()
            } catch (ignored: IOException) {
            }

        nioEventLoopGroup.shutdownGracefully()

        pool.destroy()

        timer.stop()
    }

    init {
        nioEventLoopGroup = NioEventLoopGroup()

        builder = DefaultAsyncHttpClientConfig.Builder()
        timer = HashedWheelTimer()
        timer.start()
        builder.setNettyTimer(timer)
        builder.setThreadFactory(DefaultThreadFactory("asyncPool"))
        builder.setEventLoopGroup(nioEventLoopGroup)
        builder.setSslEngineFactory(DefaultSslEngineFactory())

        pool = DefaultChannelPool(Integer.MAX_VALUE, Integer.MAX_VALUE, timer, Integer.MAX_VALUE)
        builder.setChannelPool(pool)

        builder.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT)
    }
}
