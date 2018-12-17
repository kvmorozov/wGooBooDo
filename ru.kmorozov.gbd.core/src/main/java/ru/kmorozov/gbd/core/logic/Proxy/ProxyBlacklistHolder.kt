package ru.kmorozov.gbd.core.logic.Proxy

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

/**
 * Created by km on 29.10.2016.
 */
class ProxyBlacklistHolder private constructor() {
    private val blacklistFile: File
    private val storedHosts = CopyOnWriteArrayList<HttpHostExt>()

    val whiteList: Collection<String>
        get() = storedHosts.parallelStream().filter { it.isAvailable }.map<String> { it.proxyStringShort }.collect(Collectors.toList())

    init {
        blacklistFile = File(System.getProperty("java.io.tmpdir") + File.separator + BL_FILE_NAME)
        if (!blacklistFile.exists())
            try {
                blacklistFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        val charset = Charset.forName("UTF-8")
        try {
            Files.newBufferedReader(blacklistFile.toPath(), charset).use { reader ->
                do {
                    val line: String? = reader.readLine()

                    if (line == null)
                        break;

                    storedHosts.add(HttpHostExt.getProxyFromString(line))
                } while (true)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isProxyInBlacklist(proxyStr: String): Boolean {
        val host = storedHosts.parallelStream().filter { httpHostExt -> httpHostExt.isSameAsStr(proxyStr) }.findFirst()
        return host.filter { httpHostExt -> !httpHostExt.isAvailable }.isPresent
    }

    fun updateBlacklist(currentProxyList: Iterable<HttpHostExt>) {
        for (proxy in currentProxyList) {
            val proxyInListOpt = storedHosts.parallelStream().filter { httpHostExt -> httpHostExt == proxy }.findFirst()
            if (proxyInListOpt.isPresent) {
                proxyInListOpt.get().update(proxy)
            } else {
                storedHosts.add(proxy)
            }
        }

        val deadProxyList = storedHosts.parallelStream().filter { !it.isAvailable }.limit(DEAD_PROXY_TREASHOLD.toLong()).collect(Collectors.toList())
        val liveProxyList = storedHosts.parallelStream().filter { it.isAvailable }.limit(DEAD_PROXY_TREASHOLD.toLong()).collect(Collectors.toList())

        try {
            PrintWriter(blacklistFile!!).use { out ->
                for (host in liveProxyList)
                    out.write(host.proxyString + System.getProperty("line.separator"))
                for (host in deadProxyList)
                    out.write(host.proxyString + System.getProperty("line.separator"))

                out.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    companion object {

        val BLACKLIST = ProxyBlacklistHolder()
        private const val BL_FILE_NAME = "black.lst"
        private const val DEAD_PROXY_TREASHOLD = 5000
    }
}
