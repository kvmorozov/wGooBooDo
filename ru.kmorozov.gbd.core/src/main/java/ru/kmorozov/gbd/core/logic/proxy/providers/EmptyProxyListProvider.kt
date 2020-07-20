package ru.kmorozov.gbd.core.logic.proxy.providers

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType

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
