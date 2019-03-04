package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.config.EmptyContextLoader.Companion.EMPTY_CONTEXT_LOADER
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory

class ListBasedContextLoader(private val producer: IBookListProducer) : IContextLoader {
    override val empty: Boolean
        get() = false

    override val bookIdsList: Set<String>
        get() = producer.bookIds

    override val contextSize: Int
        get() = 0

    override val isValid: Boolean
        get() = true

    override fun updateContext() {

    }

    override fun updateBookInfo(bookInfo: BookInfo) {

    }

    override fun getBookInfo(bookId: String): BookInfo {
        return LibraryFactory.getMetadata(bookId).getBookInfoExtractor(bookId, EMPTY_CONTEXT_LOADER).bookInfo
    }
}
