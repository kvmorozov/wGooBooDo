package ru.kmorozov.gbd.core.logic.connectors.ok

import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Created by km on 17.05.2016.
 */
class OkHttpConnector : HttpConnector() {

    private fun getFactory(proxy: HttpHostExt, withTimeout: Boolean): Call.Factory {
        val key = getProxyKey(proxy)

        val factoryMap = if (withTimeout) httpFactoryMapWithTimeout else httpFactoryMap

        var requestFactory: OkHttpClient? = factoryMap[key]

        if (null == requestFactory)
            synchronized(proxy) {
                requestFactory = OkHttpClient.Builder().proxy(proxy.proxy).connectTimeout((if (withTimeout) HttpConnector.CONNECT_TIMEOUT else HttpConnector.CONNECT_TIMEOUT * 10).toLong(), TimeUnit.MILLISECONDS).build()

                factoryMap.put(key, requestFactory!!)
            }

        return requestFactory!!
    }

    @Throws(IOException::class)
    override fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response? {
        if (GBDOptions.secureMode() && proxy.isLocal || !proxy.isAvailable) return null

        val googleHeaders = proxy.getHeaders(getUrlType(url))
        val headerItems = ArrayList<String>()
        for ((key, value) in googleHeaders) {
            headerItems.add(key)
            headerItems.add(value.toString())
        }

        val okHeaders = Headers.of(*headerItems.toTypedArray())

        val request = Builder().url(url).headers(okHeaders).build()
        val response = getContent(request, proxy, withTimeout, 0)

        return OkResponse(response!!)
    }

    override fun close() {

    }


    @Throws(IOException::class)
    private fun getContent(request: Request, proxy: HttpHostExt, withTimeout: Boolean, attempt: Int): okhttp3.Response? {
        var attempt = attempt
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) return null

        if (0 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, request.url().toString()))
                Thread.sleep((HttpConnector.SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return getFactory(proxy, withTimeout).newCall(request).execute()
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(request, proxy, withTimeout, ++attempt)
        }

    }

    companion object {
        private val logger = Logger.getLogger(HttpConnector::class.java)
        private val httpFactoryMap = ConcurrentHashMap<String, OkHttpClient>()
        private val httpFactoryMapWithTimeout = ConcurrentHashMap<String, OkHttpClient>()
    }
}