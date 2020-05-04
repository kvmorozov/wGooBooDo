package ru.kmorozov.gbd.core.logic.proxy

import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Created by km on 27.11.2015.
 */
class StaticProxyListProvider internal constructor() : AbstractProxyListProvider() {

    private fun buildList() {
        try {
            Thread.currentThread().contextClassLoader.getResourceAsStream(PROXY_LIST_RES)!!
                    .use { stream -> this.proxyItems = String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().toMutableSet() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun findCandidates() {
        buildList()
    }

    override fun updateProxyList() {

    }

    companion object {

        private const val PROXY_LIST_RES = "proxy/list1"
    }
}
