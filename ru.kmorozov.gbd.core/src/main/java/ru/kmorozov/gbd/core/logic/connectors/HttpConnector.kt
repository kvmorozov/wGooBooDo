package ru.kmorozov.gbd.core.logic.connectors

import com.google.common.base.Strings
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Created by km on 17.05.2016.
 */
abstract class HttpConnector : AutoCloseable {

    @Throws(IOException::class)
    abstract fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response

    @Throws(IOException::class)
    fun getHtmlDocument(url: String, proxy: HttpHostExt, withTimeout: Boolean): Document {
        try {
            val response = getContent(url, proxy, withTimeout)

            return parser.parseInput(InputStreamReader(response.content), url)
        } finally {
            proxy.updateTimestamp()
        }
    }

    @Throws(IOException::class)
    fun getJsonMapDocument(url: String, proxy: HttpHostExt, withTimeout: Boolean): Map<String, String> {
        try {
            val response = getContent(url, proxy, withTimeout)

            return Mapper.gson.fromJson(String(response.content.readAllBytes(), Charset.forName("UTF-8")), Mapper.mapType)
        } finally {
            proxy.updateTimestamp()
        }
    }

    @Throws(IOException::class)
    fun getString(url: String, proxy: HttpHostExt, withTimeout: Boolean): String {
        try {
            val response = getContent(url, proxy, withTimeout)

            return String(response.content.readAllBytes(), Charset.forName("UTF-8"))
        } finally {
            proxy.updateTimestamp()
        }
    }

    fun getUrlType(url: String): UrlType {
        return if (url.contains("frontcover"))
            UrlType.GOOGLE_BOOK_INFO
        else if (url.contains("books.google"))
            UrlType.GOOGLE_BOOKS
        else if (url.contains("jstor"))
            UrlType.JSTOR
        else
            UrlType.OTHER
    }

    protected fun needHeaders(url: String): Boolean {
        return getUrlType(url) == UrlType.GOOGLE_BOOKS || getUrlType(url) == UrlType.GOOGLE_BOOK_INFO
    }

    protected fun validateProxy(url: String, proxy: HttpHostExt): Boolean {
        if (!needHeaders(url))
            return true

        val urlType = getUrlType(url)

        val headers = proxy.getHeaders(urlType)

        if (Strings.isNullOrEmpty(headers.cookie))
            return false

        return when (urlType) {
            UrlType.GOOGLE_BOOKS -> headers.cookie.contains("NID")
            UrlType.JSTOR -> headers.cookie.contains("UUID")
            else -> true
        }
    }


    protected fun getProxyKey(proxy: HttpHostExt): String {
        return proxy.toString()
    }

    companion object {

        const val CONNECT_TIMEOUT = 30000
        private val logger = Logger.getLogger(GBDOptions.debugEnabled, HttpConnector::class.java)
        const val MAX_RETRY_COUNT = 5
        const val SLEEP_TIME = 500

        private val parser = Parser.htmlParser()
    }
}
