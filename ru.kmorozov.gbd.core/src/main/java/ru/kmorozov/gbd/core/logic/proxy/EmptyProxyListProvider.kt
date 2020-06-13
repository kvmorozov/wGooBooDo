package ru.kmorozov.gbd.core.logic.proxy

import ru.kmorozov.gbd.core.logic.context.ExecutionContext

class EmptyProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.NO_PROXY)
    }

    override fun updateProxyList() {

    }

    override fun processProxyList(urlType: UrlType) {
        ExecutionContext.sendProxyEvent(HttpHostExt.NO_PROXY)
    }

    companion object {

        val INSTANCE = EmptyProxyListProvider()
    }
}
