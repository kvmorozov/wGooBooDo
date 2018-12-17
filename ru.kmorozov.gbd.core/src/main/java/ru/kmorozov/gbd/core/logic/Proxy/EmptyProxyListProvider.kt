package ru.kmorozov.gbd.core.logic.Proxy

class EmptyProxyListProvider private constructor() : AbstractProxyListProvider() {

    init {
        proxyList.add(HttpHostExt.NO_PROXY)

        proxyListCompleted.set(true)
    }

    override fun updateProxyList() {

    }

    companion object {

        val INSTANCE = EmptyProxyListProvider()
    }
}
