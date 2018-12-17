package ru.kmorozov.gbd.core.logic.Proxy

import org.apache.commons.io.IOUtils
import ru.kmorozov.gbd.core.config.GBDOptions

import java.io.*
import java.util.HashSet

/**
 * Created by sbt-morozov-kv on 02.12.2015.
 */
class FileProxyListProvider internal constructor() : AbstractProxyListProvider() {

    init {
        buildList()
    }

    private fun buildList() {
        val proxyListFileName = GBDOptions.proxyListFile
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return

        val proxyListFile = File(proxyListFileName)
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return

        try {
            FileInputStream(proxyListFile).use { `is` -> this.proxyItems = HashSet(IOUtils.readLines(`is`, "UTF-8")) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun updateProxyList() {
        val proxyListFileName = GBDOptions.proxyListFile
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return

        val proxyListFile = File(proxyListFileName)
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return

        try {
            FileOutputStream(proxyListFile, false).use { os ->
                for (proxy in proxyList)
                    if (!proxy.isLocal && proxy.isAvailable) IOUtils.write(String.format("%s %s%n", proxy.host.hostName, proxy.host.port), os, "UTF-8")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
