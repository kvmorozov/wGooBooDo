package ru.kmorozov.gbd.core.logic.proxy.providers

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor

class EmptyProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.NO_PROXY)
    }

    override fun updateProxyList() {

    }

    override fun processProxyList(urlType: UrlType) {
        ExecutionContext.proxyExecutor = QueuedThreadPoolExecutor(proxyItems.size, 5, { true }, "proxyExecutor")
        ExecutionContext.sendProxyEvent(HttpHostExt.NO_PROXY)
    }

    companion object {

        val INSTANCE = EmptyProxyListProvider()
    }
}
