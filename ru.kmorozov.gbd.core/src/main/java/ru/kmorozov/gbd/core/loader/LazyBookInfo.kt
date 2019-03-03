package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.config.EmptyContextLoader.Companion.EMPTY_CONTEXT_LOADER
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class LazyBookInfo : BookInfo {
    override val bookId: String

    private var loaded: Boolean = false

    constructor(bookId: String) : super(EMPTY_BOOK.bookData, EMPTY_BOOK.pages, bookId) {
        this.bookId = bookId
    }

    override var lastPdfChecked: Long = 0

    override val empty: Boolean
        get() = false

    var realBookInfo: BookInfo? = null

    override val bookData: IBookData
        get() = getOrLoadBookInfo().bookData
    override val pages: IPagesInfo
        get() = getOrLoadBookInfo().pages


    private fun getOrLoadBookInfo(): BookInfo {
        if (!loaded) {
            realBookInfo = LibraryFactory.getMetadata(bookId).getBookInfoExtractor(bookId, EMPTY_CONTEXT_LOADER).bookInfo

            loaded = true
        }

        return realBookInfo!!
    }
}