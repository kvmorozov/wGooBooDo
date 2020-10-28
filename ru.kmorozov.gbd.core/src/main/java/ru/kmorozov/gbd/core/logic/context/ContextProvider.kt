package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
class ContextProvider(protected var loader: IIndex) : IContextLoader {
    override val empty: Boolean
        get() = false

    override val bookIdsList: Set<String>
        get() = loader.bookIdsList

    override val contextSize: Int
        get() = loader.books.size

    override val isValid: Boolean
        get() = GBDOptions.isValidConfig

    override fun updateContext() {
        loader.updateContext()
    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        loader.updateBook(bookInfo)
    }

    override fun getBookInfo(bookId: String): IBookInfo {
        return loader.getBookInfo(bookId)
    }

    companion object {
        var contextProvider: IContextLoader =
                ContextProvider(GBDOptions.storage.getIndex(GBDOptions.ctxOptions.connectionParams, false))
    }
}
