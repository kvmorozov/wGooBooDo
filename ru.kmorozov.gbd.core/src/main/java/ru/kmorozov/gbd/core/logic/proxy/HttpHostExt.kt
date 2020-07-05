package ru.kmorozov.gbd.core.logic.proxy

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport.Builder
import com.google.common.base.Strings
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.HttpConnections
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.Type
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by km on 29.11.2015.
 */
class HttpHostExt {
    private val available: AtomicBoolean
    private val failureCount: AtomicInteger

    var host: InetSocketAddress
        private set
    public var proxy: Proxy
        private set

    var cookie: String? = null
    var isSecure = true
        private set

    @Volatile
    private var headers: HttpHeaders? = null

    @Volatile
    var lastUsedTimestamp: Long = 0
        private set

    val isAvailable: Boolean
        get() = available.get()

    val isLocal: Boolean
        get() = this === NO_PROXY

    val proxyString: String
        get() = host.toString() + ";" + failureCount.get()

    val proxyStringShort: String
        get() = host.toString()

    constructor(host: InetSocketAddress, cookie: String) {
        this.cookie = cookie

        this.host = host
        proxy = Proxy(Type.HTTP, host)

        if (GBDOptions.secureMode) isSecure = checkSecurity()

        failureCount = AtomicInteger(0)
        available = AtomicBoolean(true)
    }

    internal constructor(host: InetSocketAddress, failureCount: Int) {
        this.failureCount = AtomicInteger(failureCount)

        this.host = host
        proxy = Proxy(Type.HTTP, host)

        available = AtomicBoolean(REMOTE_FAILURES_THRESHOLD >= failureCount)
    }

    private constructor() : this(InetSocketAddress("localhost", 1), Proxy.NO_PROXY)

    private constructor(host: InetSocketAddress, proxy: Proxy) {
        this.proxy = proxy
        this.host = host

        failureCount = AtomicInteger(0)
        available = AtomicBoolean(true)
    }

    private fun checkSecurity(): Boolean {
        val requestFactory = Builder().setProxy(proxy).build().createRequestFactory()

        try {
            val resp = requestFactory.buildGetRequest(checkProxyUrl).execute()
            resp?.content?.use { stream ->
                val respStr = String(stream.readAllBytes())
                return !respStr.contains(InetAddress.getLocalHost().hostName)
            }
        } catch (e: IOException) {
            return true
        }

        return false
    }

    override fun hashCode(): Int {
        return (host.hostName + host.port).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return !(null == other || other !is HttpHostExt) && host == other.host
    }

    override fun toString(): String {
        return String.format("%s (%d)", if (host.port == 1) NO_PROXY_STR else host.toString(), -1 * failureCount.get())
    }

    fun registerFailure() {
        if (!isAvailable) return

        failureCount.incrementAndGet()
        if (failureCount.get() > (if (isLocal) LOCAL_FAILURES_THRESHOLD else REMOTE_FAILURES_THRESHOLD)) {
            synchronized(this) {
                if (isAvailable) {
                    logger.info("Proxy ${if (host.port == 1) NO_PROXY_STR else host.toString()} invalidated!")
                    available.set(false)
                    AbstractProxyListProvider.INSTANCE.invalidatedProxyListener()
                }
            }
        }
    }

    fun forceInvalidate(reportFailure: Boolean) {
        synchronized(this) {
            if (isAvailable) {
                failureCount.addAndGet(5)
                available.set(false)
                if (reportFailure)
                    logger.info("Proxy ${if (host.port == 1) NO_PROXY_STR else host.toString()} force-invalidated!")

                AbstractProxyListProvider.INSTANCE.invalidatedProxyListener()
            }
        }
    }

    fun promoteProxy() {
        if (!isLocal) failureCount.decrementAndGet()
    }

    fun isSameAsStr(proxyStr: String): Boolean {
        return !Strings.isNullOrEmpty(proxyStr) && proxyStr == proxyStringShort
    }

    fun update(anotherHost: HttpHostExt) {
        failureCount.set(failureCount.get() + anotherHost.failureCount.get())
    }

    fun getHeaders(urlType: UrlType): HttpHeaders {
        if (null == headers || null == headers!!.cookie) {
            synchronized(this) {
                if (null == headers || Strings.isNullOrEmpty(headers!!.cookie)) {
                    headers = HttpConnections.getHeaders(this)
                    if (Strings.isNullOrEmpty(headers!!.cookie)) headers!!.cookie =
                            HttpConnections.getCookieString(host, urlType)
                    if (Strings.isNullOrEmpty(headers!!.cookie)) {
                        logger.severe("Cannot get cookies for proxy $this")
                        forceInvalidate(false)
                    }
                }
            }
        }

        return headers!!
    }

    fun updateTimestamp() {
        lastUsedTimestamp = System.currentTimeMillis()
    }

    companion object {

        val NO_PROXY = HttpHostExt()
        val TOR_PROXY = HttpHostExt(InetSocketAddress("localhost", 9150), Proxy(Type.SOCKS, InetSocketAddress("localhost", 9150)))

        private val logger = Logger.getLogger(HttpHostExt::class.java)
        private val checkProxyUrl = GenericUrl("http://mxtoolbox.com/WhatIsMyIP/")
        private const val REMOTE_FAILURES_THRESHOLD = 15
        private const val LOCAL_FAILURES_THRESHOLD = 50
        private const val NO_PROXY_STR = "NO_PROXY"

        fun getProxyFromString(proxyStr: String): HttpHostExt {
            try {
                val proxyVars = proxyStr.split("[;:]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return HttpHostExt(InetSocketAddress.createUnresolved(proxyVars[0], Integer.parseInt(proxyVars[1])), Integer.parseInt(proxyVars[2]))
            } catch (ex: Exception) {
                logger.error("Failed load proxyStr ${proxyStr}")
                return NO_PROXY
            }
        }
    }
}
