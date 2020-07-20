package ru.kmorozov.gbd.core.logic.proxy.providers

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType

class TorProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.TOR_PROXY)
    }

    override fun updateProxyList() {

    }

    override fun processProxyList(urlType: UrlType) {
        ExecutionContext.sendProxyEvent(HttpHostExt.TOR_PROXY)
    }

    companion object {

        val INSTANCE = TorProxyListProvider()
    }
}
