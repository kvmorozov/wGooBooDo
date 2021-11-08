package ru.kmorozov.gbd.utils

import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class SimpleProxySelector(proxy: Proxy) : ProxySelector() {

    var list : MutableList<Proxy>

    init {
        list = java.util.List.of(proxy)
    }

    override fun select(uri: URI?): MutableList<Proxy> {
        return list
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {

    }
}