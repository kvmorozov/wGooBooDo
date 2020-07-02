package ru.kmorozov.gbd.core.logic.proxy

import ru.kmorozov.gbd.core.logic.context.ExecutionContext

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
