package ru.kmorozov.gbd.core.logic.proxy.web

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jsoup.nodes.Document
import java.util.stream.Collectors

class JsonIpExtractor : AbstractProxyExtractor() {

    override val proxyListUrl: String
        get() = PROXY_LIST_URL

    override fun extractProxyList(doc: Document): MutableList<String> {
        return JsonParser.parseString(doc.body().text()).asJsonObject["data"].asJsonArray
            .map { extractProxyData(it.asJsonObject) }.stream().collect(Collectors.toList())
    }

    companion object {

        private const val PROXY_LIST_URL =
            "https://proxylist.geonode.com/api/proxy-list?limit=50&page=2&sort_by=lastChecked&sort_type=desc&protocols=https"

        private fun extractProxyData(element: JsonObject): String {
            return element["ip"].asString + ":" + element["port"].asString
        }
    }
}