package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
class DirContextLoader : IContextLoader {
    override val empty: Boolean
        get() = false

    protected val loadedFileName: String
        get() = GBDOptions.ctxOptions.connectionParams

    override val bookIdsList: Set<String>
        get() = getIndex(false).bookIdsList

    val books: Iterable<IBookInfo>
        get() = getIndex(false).books

    override val contextSize: Int
        get() = getIndex(false).books.size

    override val isValid: Boolean
        get() = GBDOptions.isValidConfig

    init {
        if (GBDOptions.isValidConfig)
            updateContext()
    }

    override fun updateContext() {
        getIndex(false).updateContext()
    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        throw UnsupportedOperationException()
    }

    override fun getBookInfo(bookId: String): IBookInfo {
        return getIndex(false).getBookInfo(bookId)
    }

    protected fun getIndex(createIfNotExists: Boolean): IIndex {
        return GBDOptions.storage.getIndex(loadedFileName, createIfNotExists)
    }

    companion object {
        val BOOK_CTX_LOADER = DirContextLoader()
    }
}
