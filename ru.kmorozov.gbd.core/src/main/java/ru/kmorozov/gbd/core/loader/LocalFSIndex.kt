package ru.kmorozov.gbd.core.loader

import com.google.common.base.Strings
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import kotlin.streams.toList

open class LocalFSIndex : IIndex {

    private val storage: LocalFSStorage

    constructor(storage: LocalFSStorage) {
        this.storage = storage
        this.booksMap = emptyMap<String, IBookInfo>().toMutableMap()
    }

    protected var loaded: Boolean = false

    protected var booksMap: MutableMap<String, IBookInfo>

    override val books: List<IBookInfo>
        get() = getOrLoadBooks()

    override val bookIdsList: Set<String>
        get() = if (loaded) booksMap.keys else getOrLoadBooks().map { it.bookId }.toSet()

    open protected fun getOrLoadBooks(): List<IBookInfo> {
        if (!loaded) {
            booksMap = getFromStorage().associateBy { it.bookId }.toMutableMap()

            loaded = true
        }

        return booksMap.values.toList()
    }

    override fun updateIndex(books: List<IBookInfo>) {

    }

    private fun getFromStorage(): List<IBookInfo> {
        var result = mutableListOf<LazyBookInfo>()
        return storage.bookIdsList.mapTo(result, { bookId -> LazyBookInfo(bookId, this) })
    }

    override fun updateBook(book: IBookInfo) {
        booksMap.put(book.bookId, book)
    }

    override fun updateContext() {
        if (!Strings.isNullOrEmpty(GBDOptions.bookId)) return

        if (ExecutionContext.initialized) {
            booksMap = ExecutionContext.INSTANCE.getContexts(false).stream()
                    .map(BookContext::bookInfo).toList().associate({ it.bookId to it }).toMutableMap()
            updateIndex(ArrayList<IBookInfo>(booksMap.values))
        } else {
            booksMap = getFromStorage().associate({ it.bookId to it }).toMutableMap()
            booksMap.toMutableMap().putAll(books.associate({ it.bookId to it }))
        }
    }

    override fun getBookInfo(bookId: String): IBookInfo {
        return booksMap.getOrDefault(bookId, BookInfo.EMPTY_BOOK)
    }
}
