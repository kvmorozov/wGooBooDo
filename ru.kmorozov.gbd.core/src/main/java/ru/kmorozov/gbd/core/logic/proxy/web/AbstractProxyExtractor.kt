package ru.kmorozov.gbd.core.logic.proxy.web

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.kmorozov.gbd.utils.HttpConnections

import java.io.IOException
import java.util.ArrayList

/**
 * Created by km on 17.12.2016.
 */
abstract class AbstractProxyExtractor {

    val proxyList: MutableList<String>
        get() {
            try {
                val doc = Jsoup.connect(proxyListUrl).userAgent(HttpConnections.USER_AGENT).ignoreContentType(true).get()
                return extractProxyList(doc)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return ArrayList()
        }

    protected abstract val proxyListUrl: String

    protected abstract fun extractProxyList(doc: Document): MutableList<String>
}
