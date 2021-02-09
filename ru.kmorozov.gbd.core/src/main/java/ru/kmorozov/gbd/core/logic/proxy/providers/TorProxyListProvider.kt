package ru.kmorozov.gbd.core.logic.proxy.providers

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor

class TorProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.TOR_PROXY)
    }

    override fun updateProxyList() {

    }

    override fun processProxyList(urlType: UrlType) {
        ExecutionContext.proxyExecutor = QueuedThreadPoolExecutor(proxyItems.size, 5, { true }, "proxyExecutor")
        ExecutionContext.sendProxyEvent(HttpHostExt.TOR_PROXY)
    }

    companion object {

        val INSTANCE = TorProxyListProvider()
    }
}
