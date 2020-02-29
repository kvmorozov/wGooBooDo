package ru.kmorozov.gbd.core.logic.extractors.base

import org.jsoup.Connection
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.BookInfo.Companion.EMPTY_BOOK
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.utils.HttpConnections
import java.io.IOException
import java.net.UnknownHostException
import java.nio.charset.Charset

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractBookInfoExtractor : AbstractHttpProcessor {

    protected val logger = ExecutionContext.getLogger(AbstractBookInfoExtractor::class.java)

    protected lateinit var bookId: String
    lateinit var bookInfo: BookInfo
        protected set

    private val storedLoader: IContextLoader

    protected abstract val bookUrl: String

    protected open val reserveBookUrl: String
        get() = bookUrl

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

    constructor(storedLoader: IContextLoader) {
        this.storedLoader = storedLoader
    }

    @JvmOverloads
    protected constructor(bookId: String, storedLoader: IContextLoader = ContextProvider.contextProvider) {
        this.bookId = bookId
        this.storedLoader = storedLoader

        val storedBookInfo = if (storedLoader.empty) EMPTY_BOOK else storedLoader.getBookInfo(bookId)
        bookInfo = if (storedBookInfo.empty) findBookInfo() else storedBookInfo as BookInfo
    }

    open fun findBookInfo(): BookInfo {
        logger.info("Loading bookinfo for $bookId...")
        try {
            return extractBookInfo(documentWithoutProxy)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return EMPTY_BOOK
    }

    protected abstract fun extractBookInfo(doc: Document?): BookInfo

    protected fun getDocumentWithProxy(proxy: HttpHostExt): Document? {
        val resp = getContent(bookUrl, proxy, true)

        if (resp.empty)
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
}
