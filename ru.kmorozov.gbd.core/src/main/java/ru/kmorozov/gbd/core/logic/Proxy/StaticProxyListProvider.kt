package ru.kmorozov.gbd.core.logic.Proxy

import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

/**
 * Created by km on 27.11.2015.
 */
class StaticProxyListProvider internal constructor() : AbstractProxyListProvider() {

    init {
        buildList()
    }

    private fun buildList() {
        try {
            Thread.currentThread().contextClassLoader.getResourceAsStream(PROXY_LIST_RES)!!
                    .use { stream -> this.proxyItems = String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().toMutableSet() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun updateProxyList() {

    }

    companion object {

        private const val PROXY_LIST_RES = "proxy/list1"
    }
}
