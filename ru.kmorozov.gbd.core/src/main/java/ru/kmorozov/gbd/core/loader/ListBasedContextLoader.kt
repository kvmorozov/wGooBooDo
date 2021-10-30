package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.config.EmptyContextLoader.Companion.EMPTY_CONTEXT_LOADER
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory

class ListBasedContextLoader(private val producer: IBookListProducer) : IContextLoader {

    private val books: MutableMap<String, BookInfo> = HashMap()

    override val empty: Boolean
        get() = false

    override val bookIdsList: Set<String>
        get() = producer.bookIds

    override val contextSize: Int
        get() = producer.bookIds.size

    override val isValid: Boolean
        get() = true

    override fun updateContext() {

    }

    override fun updateBookInfo(bookInfo: BookInfo) {

    }

    override fun getBookInfo(bookId: String): BookInfo {
        if (!books.containsKey(bookId))
            books.put(bookId, LibraryFactory.getMetadata(bookId).getBookInfoExtractor(bookId, EMPTY_CONTEXT_LOADER).bookInfo)

        return books.get(bookId)!!
    }
}
