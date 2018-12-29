package ru.kmorozov.gbd.core.loader

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import java.util.*
import java.util.stream.Collectors

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
class DirContextLoader : IContextLoader {
    private val booksInfo = HashMap<String, IBookInfo>()

    protected val loadedFileName: String
        get() = GBDOptions.ctxOptions.connectionParams

    override val bookIdsList: Set<String>
        get() = if (booksInfo.isEmpty()) HashSet() else booksInfo.keys

    val books: Iterable<IBookInfo>
        get() = booksInfo.values

    override val contextSize: Int
        get() = booksInfo.size

    override val isValid: Boolean
        get() = GBDOptions.isValidConfig

    init {
        initContext()
    }

    override fun updateIndex() {

    }

    override fun updateContext() {
        if (!StringUtils.isEmpty(GBDOptions.bookId)) return

        val runtimeBooksInfo = if (ExecutionContext.initialized)
            ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::bookInfo).collect(Collectors.toList())
        else getFromStorage()

        for (bookInfo in runtimeBooksInfo)
            booksInfo[bookInfo.bookId] = bookInfo

        getIndex(true).updateIndex(ArrayList<IBookInfo>(booksInfo.values))
    }

    private fun getFromStorage(): List<IBookInfo> {
        var result = mutableListOf<LazyBookInfo>()
        return GBDOptions.storage.bookIdsList.mapTo(result, { bookId -> LazyBookInfo(bookId) })
    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        throw UnsupportedOperationException()
    }

    private fun initContext() {
        if (!GBDOptions.isValidConfig) return

        refreshContext()
    }

    override fun getBookInfo(bookId: String): IBookInfo {
        return booksInfo.getOrDefault(bookId, BookInfo.EMPTY_BOOK)
    }

    override fun refreshContext() {
        val index = getIndex(false)

        val ctxObjArr = index.books

        for (ctxObj in ctxObjArr)
            if (ctxObj is BookInfo) {
                val bookInfo = ctxObj
                booksInfo[bookInfo.bookId] = bookInfo
            }
    }

    protected fun getIndex(createIfNotExists: Boolean): IIndex {
        return GBDOptions.storage.getIndex(loadedFileName, createIfNotExists)

    }

    companion object {

        val BOOK_CTX_LOADER = DirContextLoader()
    }
}
