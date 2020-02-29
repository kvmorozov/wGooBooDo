package ru.kmorozov.gbd.core.logic.proxy

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.GBDOptions
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors

/**
 * Created by sbt-morozov-kv on 02.12.2015.
 */
class FileProxyListProvider internal constructor() : AbstractProxyListProvider() {

    init {
        val proxyListFileName = GBDOptions.proxyListFile
        if (StringUtils.isEmpty(proxyListFileName)) {
            val proxyListFile = File(proxyListFileName)
            if (proxyListFile.exists() && proxyListFile.canRead())
                this.proxyItems = Files.lines(proxyListFile.toPath()).collect(Collectors.toSet())
        }
    }

    override fun updateProxyList() {
        val proxyListFileName = GBDOptions.proxyListFile
        if (StringUtils.isEmpty(proxyListFileName)) return

        val proxyListFile = File(proxyListFileName)
        val proxyListPath = proxyListFile.toPath()
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return

        Files.write(proxyListPath,
                proxyList.filter { !it.isLocal && it.isAvailable }.map { String.format("%s %s%n", it.host.hostName, it.host.port) })
    }
}
