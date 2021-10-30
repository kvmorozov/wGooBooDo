package ru.kmorozov.gbd.core.logic.proxy.providers

import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import java.util.*

class ManagedProxyListProvider(private val parentProvider: IProxyListProvider, private val timeout: Int) {

    val proxy: HttpHostExt
        get() {
            if (checkReady(HttpHostExt.NO_PROXY))
                return HttpHostExt.NO_PROXY

            parentProvider.processProxyList(UrlType.JSTOR)

            var opProxy: Optional<HttpHostExt>
            do {
                try {
                    Thread.sleep(100L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                opProxy = parentProvider
                        .parallelProxyStream
                        .filter { this.checkReady(it) }
                        .filter { it.isAvailable }
                        .findFirst()
            } while (!opProxy.isPresent)

            return opProxy.get()
        }

    constructor(timeout: Int) : this(AbstractProxyListProvider.INSTANCE, timeout)

    private fun checkReady(proxy: HttpHostExt): Boolean {
        return System.currentTimeMillis() - proxy.lastUsedTimestamp > timeout.toLong()
    }
}
