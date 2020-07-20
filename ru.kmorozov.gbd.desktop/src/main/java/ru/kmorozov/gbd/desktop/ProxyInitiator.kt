package ru.kmorozov.gbd.desktop

import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.UrlType

class ProxyInitiator {

    fun proxyInit() {
        AbstractProxyListProvider.INSTANCE.reset()
        AbstractProxyListProvider.INSTANCE.findCandidates()
        AbstractProxyListProvider.INSTANCE.processProxyList(UrlType.GOOGLE_BOOKS)
    }
}