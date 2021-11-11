package ru.kmorozov.gbd.core.logic.proxy.providers

import com.google.common.base.Strings
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.core.logic.proxy.web.WebProxyListProvider
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.HttpConnections
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import ru.kmorozov.gbd.utils.SetBlockingQueue
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.stream.Stream

/**
 * Created by km on 27.11.2015.
 */

abstract class AbstractProxyListProvider : IProxyListProvider {

    override val proxyList: LinkedBlockingQueue<HttpHostExt> = SetBlockingQueue()
    protected var proxyItems: MutableSet<Optional<InetSocketAddress>> = HashSet()

    init {
        initList()
    }

    open fun initList() {}

    override val parallelProxyStream: Stream<HttpHostExt>
        get() = proxyList.parallelStream()

    override val proxyCount: Int
        get() = proxyList.size

    fun getSomeProxy(): HttpHostExt {
        if (proxyList.isEmpty()) {
            val proxy = proxyList.take()
            proxyList.add(proxy)
            return proxy
        } else
            return proxyList.element()
    }

    private fun splitItems(proxyItem: String): Array<String> {
        var tmpItems = splitItems(proxyItem, DEFAULT_PROXY_DELIMITER)
        if (2 <= tmpItems.size)
            return tmpItems
        else {
            tmpItems = splitItems(proxyItem, "\\s+")
            return if (2 <= tmpItems.size) tmpItems else emptyArray()
        }
    }

    override fun processProxyList(urlType: UrlType) {
        ExecutionContext.proxyExecutor = QueuedThreadPoolExecutor(proxyItems.size, 5, { true }, "proxyExecutor")
        proxyItems.forEach {
            ExecutionContext.proxyExecutor.execute {
                val opHost = processProxyItem(it.get(), urlType)
                if (opHost.isPresent)
                    ExecutionContext.sendProxyEvent(opHost.get())
            }
        }

        ExecutionContext.sendProxyEvent(HttpHostExt.NO_PROXY)
    }

    override fun invalidatedProxyListener() {
        proxyList.removeIf { !it.isAvailable }
        val liveProxyCount = proxyList.count()
        if (0 == liveProxyCount) {
            logger.severe("No more proxies!")
            ExecutionContext.INSTANCE.forceCompleteAll()
        }
    }

    protected fun getInetAddress(proxyItem: String): Optional<InetSocketAddress> {
        val proxyItemArr = splitItems(proxyItem)
        if (2 > proxyItemArr.size)
            return Optional.empty()
        else
            return Optional.of(InetSocketAddress(proxyItemArr[0], Integer.parseInt(proxyItemArr[1])))
    }

    private fun processProxyItem(host: InetSocketAddress, urlType: UrlType): Optional<HttpHostExt> {
        val proxy: HttpHostExt

        val cookie = HttpConnections.INSTANCE.getCookieString(host, urlType)
        proxy = HttpHostExt(host, cookie)

        if (!Strings.isNullOrEmpty(cookie)) {
            if (!GBDOptions.secureMode || proxy.isSecure) {
                logger.finest("${if (GBDOptions.secureMode) if (proxy.isSecure) "Secure p" else "NOT secure p" else "P"}roxy $host added.")
            } else {
                logger.finest("NOT secure proxy $host NOT added.")
                proxy.forceInvalidate(false)
            }
        } else {
            logger.finest("Proxy $host NOT added.")
            proxy.forceInvalidate(false)
        }

        if (proxy.isAvailable) {
            proxyList.add(proxy)
            return Optional.of(proxy)
        } else
            return Optional.empty()
    }

    protected fun notBlacklisted(proxyStr: String): Boolean {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr)
    }

    fun reset() {
        proxyList.removeIf { !it.isAvailable }
    }

    abstract fun findCandidates()

    companion object {

        private const val DEFAULT_PROXY_DELIMITER = ":"

        private val logger = Logger.getLogger(GBDOptions.debugEnabled, AbstractProxyListProvider::class.java)

        val INSTANCE: AbstractProxyListProvider
            get() = if (Strings.isNullOrEmpty(GBDOptions.proxyListFile))
                EmptyProxyListProvider.INSTANCE
            else if (GBDOptions.proxyListFile.equals("web", ignoreCase = true))
                WebProxyListProvider.INSTANCE
            else if (GBDOptions.proxyListFile.equals("tor", ignoreCase = true))
                TorProxyListProvider.INSTANCE
            else FileProxyListProvider.INSTANCE

        fun updateBlacklist() {
            if (INSTANCE.proxyList.size > 1)
                ProxyBlacklistHolder.BLACKLIST.updateBlacklist(INSTANCE.proxyList)
        }

        private fun splitItems(proxyItem: String, delimiter: String): Array<String> {
            return proxyItem.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
    }
}
