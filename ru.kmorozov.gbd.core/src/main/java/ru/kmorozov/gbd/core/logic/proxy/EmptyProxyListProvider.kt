package ru.kmorozov.gbd.core.logic.proxy

class EmptyProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.NO_PROXY)

        proxyListCompleted.set(true)
    }

    override fun updateProxyList() {

    }

    companion object {

        val INSTANCE = EmptyProxyListProvider()
    }
}
