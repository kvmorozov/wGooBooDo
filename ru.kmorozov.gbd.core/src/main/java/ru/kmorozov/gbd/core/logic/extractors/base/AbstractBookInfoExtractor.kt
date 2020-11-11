package ru.kmorozov.gbd.core.logic.extractors.base

import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.BookInfo.Companion.EMPTY_BOOK
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.utils.HttpConnections
import java.util.*

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

    protected val rawDocument: Optional<Document>
        get() {
            if (GBDOptions.secureMode)
                return getDocumentWithProxy(AbstractProxyListProvider.INSTANCE.getSomeProxy())
            else
                return Optional.of(Jsoup.connect(bookUrl).userAgent(HttpConnections.USER_AGENT)
                        .timeout(30000).method(Method.GET).execute().parse())
        }

    constructor(storedLoader: IContextLoader) {
        this.storedLoader = storedLoader
    }

    @JvmOverloads
    protected constructor (bookId: String, storedLoader: IContextLoader = ContextProvider.contextProvider) {
        this.bookId = bookId
        this.storedLoader = storedLoader

        val storedBookInfo = if (storedLoader.empty) EMPTY_BOOK else storedLoader.getBookInfo(bookId)
        bookInfo = if (storedBookInfo.empty) findBookInfo() else storedBookInfo as BookInfo
    }

    open fun findBookInfo(): BookInfo {
        logger.info("Loading bookinfo for $bookId...")
        return if (rawDocument.isPresent) extractBookInfo(rawDocument.get()) else EMPTY_BOOK
    }

    protected abstract fun extractBookInfo(doc: Document?): BookInfo

    protected fun getDocumentWithProxy(proxy: HttpHostExt): Optional<Document> {
        return getDocumentWithProxy(bookUrl, proxy)
    }
}
