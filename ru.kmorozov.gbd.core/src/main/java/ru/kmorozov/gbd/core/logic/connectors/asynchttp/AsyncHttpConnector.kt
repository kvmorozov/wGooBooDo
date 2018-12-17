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
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by km on 22.12.2016.
 */
class AsyncHttpConnector : HttpConnector() {

    private val BUILDER_LOCK = Any()
    private val clientsMap = ConcurrentHashMap<String, AsyncHttpClient>()
    @Volatile
    private var builder: DefaultAsyncHttpClientConfig.Builder? = null
    private var nioEventLoopGroup: NioEventLoopGroup? = null
    private var pool: DefaultChannelPool? = null
    private var timer: HashedWheelTimer? = null

    private fun getClient(proxy: HttpHostExt): AsyncHttpClient {
        val key = getProxyKey(proxy)
        var client: AsyncHttpClient? = clientsMap[key]

        if (null == builder)
            synchronized(BUILDER_LOCK) {
                if (null == builder) {
                    nioEventLoopGroup = NioEventLoopGroup()

                    builder = DefaultAsyncHttpClientConfig.Builder()
                    timer = HashedWheelTimer()
                    timer!!.start()
                    builder!!.setNettyTimer(timer)
                    builder!!.setThreadFactory(DefaultThreadFactory("asyncPool"))
                    builder!!.setEventLoopGroup(nioEventLoopGroup)
                    builder!!.setSslEngineFactory(DefaultSslEngineFactory())

                    pool = DefaultChannelPool(Integer.MAX_VALUE, Integer.MAX_VALUE, timer, Integer.MAX_VALUE)
                    builder!!.setChannelPool(pool)

                    builder!!.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT)
                }
            }

        if (null == client)
            synchronized(proxy) {
                if (!proxy.isLocal)
                    builder!!.setProxyServer(Builder(proxy.host.hostName, proxy.host.port).build())
                client = DefaultAsyncHttpClient(builder!!.build())

                clientsMap.put(key, client as DefaultAsyncHttpClient)
            }

        return client!!
    }

    override fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response? {
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
            return AsyncHttpResponse(builder.execute(AsyncHandler(proxy)).get(HttpConnector.CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS))
        } catch (ex: InterruptedException) {
            return null
        } catch (ex: ExecutionException) {
            return null
        } catch (ex: TimeoutException) {
            return null
        } finally {
        }
    }

    override fun close() {
        for (client in clientsMap.values)
            try {
                if (!client.isClosed)
                    client.close()
            } catch (ignored: IOException) {
            }

        if (null != nioEventLoopGroup)
            nioEventLoopGroup!!.shutdownGracefully()

        if (null != pool)
            pool!!.destroy()

        if (null != timer)
            timer!!.stop()
    }
}
