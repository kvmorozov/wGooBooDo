package ru.kmorozov.gbd.core.logic.Proxy

import java.util.stream.Stream

/**
 * Created by km on 27.11.2015.
 */
interface IProxyListProvider {

    val parallelProxyStream: Stream<HttpHostExt>

    val proxyCount: Int

    val proxyList: Iterable<HttpHostExt>

    fun invalidatedProxyListener()

    fun updateProxyList()

    fun processProxyList(urlType: UrlType)

    fun proxyListCompleted(): Boolean
}
