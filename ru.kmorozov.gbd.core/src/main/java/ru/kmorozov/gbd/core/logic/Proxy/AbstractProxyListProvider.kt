package ru.kmorozov.gbd.core.logic.Proxy

import com.google.common.base.Strings
import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.web.WebProxyListProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.HttpConnections
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream

/**
 * Created by km on 27.11.2015.
 */

abstract class AbstractProxyListProvider : IProxyListProvider {

    override val proxyList: MutableCollection<HttpHostExt> = HashSet()
    protected var proxyItems: MutableSet<String> = HashSet()

    protected val proxyListCompleted = AtomicBoolean(false)
    private val proxyListInitStarted = AtomicBoolean(false)

    override val parallelProxyStream: Stream<HttpHostExt>
        get() {
            if (!proxyListCompleted.get()) {
                try {
                    Thread.sleep(500L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            return proxyList.parallelStream()
        }

    override val proxyCount: Int
        get() = proxyItems.size

    private fun splitItems(proxyItem: String): Array<String>? {
        var tmpItems = splitItems(proxyItem, DEFAULT_PROXY_DELIMITER)
        if (2 <= tmpItems.size)
            return tmpItems
        else {
            tmpItems = splitItems(proxyItem, "\\s+")
            return if (2 <= tmpItems.size) tmpItems else null
        }
    }

    override fun processProxyList(urlType: UrlType) {
        if (proxyListCompleted.get() || proxyListInitStarted.get())
            return

        proxyListInitStarted.set(true)

        for (proxyString in proxyItems)
            if (!Strings.isNullOrEmpty(proxyString))
                Thread(ProxyChecker(proxyString, urlType)).start()
    }

    override fun proxyListCompleted(): Boolean {
        return proxyListCompleted.get()
    }

    override fun invalidatedProxyListener() {
        val liveProxyCount = proxyList.stream().filter { it.isAvailable }.count()
        if (0L == liveProxyCount && GBDOptions.secureMode) throw RuntimeException("No more proxies!")
    }

    private inner class ProxyChecker internal constructor(private val proxyStr: String, private val urlType: UrlType) : Runnable {

        override fun run() {
            val host = processProxyItem(proxyStr)
            ExecutionContext.sendProxyEvent(host)
        }

        private fun getCookie(proxy: InetSocketAddress): String {
            return HttpConnections.getCookieString(proxy, urlType)
        }

        private fun processProxyItem(proxyItem: String): HttpHostExt {
            var proxy: HttpHostExt = HttpHostExt.NO_PROXY

            try {
                val proxyItemArr = splitItems(proxyItem)

                if (null == proxyItemArr || 2 > proxyItemArr.size) return HttpHostExt.NO_PROXY

                val host = InetSocketAddress(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]))
                val cookie = getCookie(host)
                proxy = HttpHostExt(host, cookie)
                if (!StringUtils.isEmpty(cookie)) {
                    if (!GBDOptions.secureMode || proxy.isSecure) {
                        logger.info(String.format("%sroxy %s added.", if (GBDOptions.secureMode) if (proxy.isSecure) "Secure p" else "NOT secure p" else "P", host.toString()))
                    } else {
                        logger.info(String.format("NOT secure proxy %s NOT added.", host.toString()))
                        proxy.forceInvalidate(false)
                    }
                } else {
                    logger.info(String.format("Proxy %s NOT added.", host.toString()))
                    proxy.forceInvalidate(false)
                }
            } catch (ex: Exception) {
                logger.info(String.format("Not valid proxy string %s.", proxyItem))
            }

            proxyList.add(proxy)
            proxyListCompleted.set(proxyList.size == proxyItems.size)

            return proxy
        }
    }

    protected fun notBlacklisted(proxyStr: String): Boolean {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr)
    }

    companion object {

        private const val DEFAULT_PROXY_DELIMITER = ":"

        private val logger = Logger.getLogger(AbstractProxyListProvider::class.java)

        public val INSTANCE: AbstractProxyListProvider
            get() = if (StringUtils.isEmpty(GBDOptions.proxyListFile))
                EmptyProxyListProvider.INSTANCE
            else if (GBDOptions.proxyListFile.equals("web", ignoreCase = true))
                WebProxyListProvider()
            else FileProxyListProvider()

        fun updateBlacklist() {
            if (INSTANCE.proxyList.size > 1)
                ProxyBlacklistHolder.BLACKLIST.updateBlacklist(INSTANCE.proxyList)
        }

        private fun splitItems(proxyItem: String, delimiter: String): Array<String> {
            return proxyItem.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
    }
}
