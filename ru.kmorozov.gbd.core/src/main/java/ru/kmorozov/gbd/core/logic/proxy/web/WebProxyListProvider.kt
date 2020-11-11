package ru.kmorozov.gbd.core.logic.proxy.web

import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.providers.ProxyBlacklistHolder
import java.util.stream.Collectors
import kotlin.streams.toList

/**
 * Created by km on 23.11.2015.
 */
class WebProxyListProvider : AbstractProxyListProvider() {

    override fun initList() {
        this.proxyItems.addAll(
                ProxyBlacklistHolder.BLACKLIST.whiteList.stream()
                        .map { getInetAddress(it) }
                        .filter { it.isPresent }
                        .toList()
        )
    }

    override fun findCandidates() {
        val candidateProxies = SslProxiesListProvider().proxyList

        this.proxyItems = candidateProxies.stream()
                .filter { notBlacklisted(it) }
                .map { getInetAddress(it) }
                .filter { it.isPresent }
                .filter { !proxyItems.contains(it) }
                .limit(PROXY_LIMIT - proxyCount).collect(Collectors.toSet())
    }

    override fun updateProxyList() {

    }

    companion object {
        private val PROXY_LIMIT = 20L
        val INSTANCE = WebProxyListProvider()
    }
}
