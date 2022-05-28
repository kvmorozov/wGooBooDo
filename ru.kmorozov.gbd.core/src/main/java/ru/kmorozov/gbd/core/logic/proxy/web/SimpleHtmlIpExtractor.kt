package ru.kmorozov.gbd.core.logic.proxy.web

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Created by km on 17.12.2016.
 */
class SimpleHtmlIpExtractor : AbstractProxyExtractor() {

    override val proxyListUrl: String
        get() = PROXY_LIST_URL

    override fun extractProxyList(doc: Document): MutableList<String> {
        val textWithProxies = doc.html().replace("<".toRegex(), "|").replace(">".toRegex(), "|")
        return Arrays.stream(textWithProxies.split("\\|".toRegex()).toTypedArray()).map { it.trim() }.filter { it.length in 11..19 }
                .filter { s -> validIpPort(s) }.collect(Collectors.toList())
    }

    private fun validIpPort(str: String): Boolean {
        return if (StringUtils.countMatches(str, ".") != 3 || !str.contains(":")) false else !checkRegexp || pattern.matcher(str).matches()
    }

    companion object {

        private const val PROXY_LIST_URL = "https://spys.one/sslproxy/"
        private const val ipPortPattern = "^([0-9]|[0-9][0-9]|[01][0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                "([0-9]|[0-9][0-9]|[01][0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                "([0-9]|[0-9][0-9]|[01][0-9][0-9]|2[0-4][0-9]|25[0-5])\\." +
                "([0-9]|[0-9][0-9]|[01][0-9][0-9]|2[0-4][0-9]|25[0-5])\\:" +
                "([1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]|" +
                "[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]|" +
                "65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5])$"
        private val pattern = Pattern.compile(ipPortPattern)
        private const val checkRegexp = true
    }
}
