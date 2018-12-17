package ru.kmorozov.gbd.core.logic.connectors

import com.google.api.client.http.HttpHeaders
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.UrlType
import ru.kmorozov.gbd.logger.Logger

import java.io.IOException
import java.io.StringReader
import java.nio.charset.Charset

/**
 * Created by km on 17.05.2016.
 */
abstract class HttpConnector : AutoCloseable {

    @Throws(IOException::class)
    abstract fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response?

    @Throws(IOException::class)
    fun getHtmlDocument(url: String, proxy: HttpHostExt, withTimeout: Boolean): Document {
        try {
            val response = getContent(url, proxy, withTimeout) ?: throw IOException("Cannot get document!")

            return parser.parseInput(StringReader(String(response.content.readAllBytes(), Charset.forName("UTF-8"))), url)
        } finally {
            proxy.updateTimestamp()
        }
    }

    @Throws(IOException::class)
    fun getJsonMapDocument(url: String, proxy: HttpHostExt, withTimeout: Boolean): Map<String, String> {
        try {
            val response = getContent(url, proxy, withTimeout) ?: throw IOException("Cannot get document!")

            return Mapper.getGson()!!.fromJson(String(response.content.readAllBytes(), Charset.forName("UTF-8")), Mapper.mapType)
        } finally {
            proxy.updateTimestamp()
        }
    }

    @Throws(IOException::class)
    fun getString(url: String, proxy: HttpHostExt, withTimeout: Boolean): String {
        try {
            val response = getContent(url, proxy, withTimeout) ?: throw IOException("Cannot get document!")

            return String(response.content.readAllBytes(), Charset.forName("UTF-8"))
        } finally {
            proxy.updateTimestamp()
        }
    }

    fun getUrlType(url: String): UrlType {
        return if (url.contains("books.google"))
            UrlType.GOOGLE_BOOKS
        else if (url.contains("jstor"))
            UrlType.JSTOR
        else
            UrlType.OTHER
    }

    protected fun needHeaders(url: String): Boolean {
        return getUrlType(url) != UrlType.OTHER
    }

    protected fun validateProxy(url: String, proxy: HttpHostExt): Boolean {
        if (!needHeaders(url))
            return true

        val urlType = getUrlType(url)

        val headers = proxy.getHeaders(urlType)

        if (StringUtils.isEmpty(headers.cookie))
            return false

        when (urlType) {
            UrlType.GOOGLE_BOOKS -> return headers.cookie.contains("NID")
            UrlType.JSTOR -> return headers.cookie.contains("UUID")
            else -> return true
        }
    }


    protected fun getProxyKey(proxy: HttpHostExt): String {
        return proxy.toString()
    }

    companion object {

        const val CONNECT_TIMEOUT = 30000
        private val logger = Logger.getLogger(HttpConnector::class.java)
        public const val MAX_RETRY_COUNT = 2
        public const val SLEEP_TIME = 500

        private val parser = Parser.htmlParser()
    }
}
