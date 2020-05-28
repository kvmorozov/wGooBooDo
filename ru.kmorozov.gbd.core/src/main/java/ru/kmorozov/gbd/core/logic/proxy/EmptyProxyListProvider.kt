package ru.kmorozov.gbd.core.logic.proxy

class EmptyProxyListProvider private constructor() : AbstractProxyListProvider() {

    override fun findCandidates() {
        proxyList.add(HttpHostExt.NO_PROXY)
    }

    override fun updateProxyList() {

    }

    companion object {

        val INSTANCE = EmptyProxyListProvider()
    }
}
