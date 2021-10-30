package ru.kmorozov.gbd.core.logic.proxy.web

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.util.stream.Collectors

/**
 * Created by km on 17.12.2016.
 */
class HideMeListProvider : AbstractProxyExtractor() {

    override val proxyListUrl: String
        get() = PROXY_LIST_URL

    override fun extractProxyList(doc: Document): MutableList<String> {
        return doc.getElementsByClass("proxy-table")[0].getElementsByTag("tbody")[0].getElementsByTag("tr")
                .stream().map<String> { extractProxyData(it) }.collect(Collectors.toList())
    }

    companion object {

        private const val PROXY_LIST_URL = "http://hideme.ru/proxy-list/?type=s#list"

        private fun extractProxyData(element: Element): String {
            return (element.childNode(0).childNode(0) as TextNode).wholeText + ':'.toString() + (element.childNode(1).childNode(0) as TextNode).wholeText
        }
    }
}
