package ru.kmorozov.gbd.core.logic.extractors.base

import org.jsoup.Connection
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.utils.HttpConnections
import java.io.IOException
import java.net.UnknownHostException
import java.nio.charset.Charset

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractBookExtractor : AbstractHttpProcessor {

    protected lateinit var bookId: String
    lateinit var bookInfo: BookInfo
        protected set

    protected abstract val bookUrl: String

    protected abstract val reserveBookUrl: String

    protected val documentWithoutProxy: Document?
        @Throws(Exception::class)
        get() {
            var res: Connection.Response? = null
            var doc: Document? = null

            try {
                res = Jsoup.connect(bookUrl).userAgent(HttpConnections.USER_AGENT).followRedirects(false).timeout(20000).method(Method.GET).execute()
            } catch (uhe: UnknownHostException) {
                logger.severe("Not connected to Internet!")
            } catch (ex: Exception) {
                try {
                    res = Jsoup.connect(reserveBookUrl).userAgent(HttpConnections.USER_AGENT).method(Method.GET).execute()
                } catch (ex1: Exception) {
                    throw Exception(ex1)
                }
            }

            try {
                if (null != res) {
                    doc = res.parse()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return doc
        }

    constructor() {}

    @JvmOverloads
    protected constructor(bookId: String, storedLoader: IContextLoader? = ContextProvider.getContextProvider()) {
        this.bookId = bookId

        val storedBookInfo = storedLoader?.getBookInfo(bookId)
        try {
            bookInfo = storedBookInfo ?: findBookInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    protected abstract fun findBookInfo(): BookInfo

    protected fun getDocumentWithProxy(proxy: HttpHostExt): Document? {
        val resp = getContent(bookUrl, proxy, true)

        if (null == resp)
            return null
        else {
            try {
                resp.content.use { `is` ->
                    val respStr = String(`is`.readAllBytes(), Charset.defaultCharset())
                    return Jsoup.parse(respStr)
                }
            } catch (e: IOException) {
                return null
            }
        }
    }

    companion object {
        protected val logger = ExecutionContext.INSTANCE.getLogger(AbstractBookExtractor::class.java)
    }
}
