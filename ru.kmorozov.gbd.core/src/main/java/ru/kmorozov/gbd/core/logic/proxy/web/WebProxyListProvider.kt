package ru.kmorozov.gbd.core.logic.proxy.web

import ru.kmorozov.gbd.core.logic.proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.ProxyBlacklistHolder
import java.util.stream.Collectors

/**
 * Created by km on 23.11.2015.
 */
class WebProxyListProvider : AbstractProxyListProvider() {

    init {
        val candidateProxies = SslProxiesListProvider().proxyList
        candidateProxies.addAll(SslProxiesListProvider().proxyList)

        this.proxyItems = candidateProxies.stream().filter { notBlacklisted(it) }.limit(20).collect(Collectors.toSet())
        this.proxyItems.addAll(ProxyBlacklistHolder.BLACKLIST.whiteList)
    }

    override fun updateProxyList() {

    }
}
