package ru.kmorozov.gbd.core.loader

import com.google.gson.annotations.Expose
import ru.kmorozov.db.core.config.EmptyContextLoader.Companion.EMPTY_CONTEXT_LOADER
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class LazyBookInfo : BookInfo {
    private val index: IIndex

    private var loaded: Boolean = false

    constructor(bookId: String, index: IIndex) : super(EMPTY_BOOK.bookData, EMPTY_BOOK.pages, bookId) {
        this.bookId = bookId
        this.index = index
    }

    override val empty: Boolean
        get() = false

    @Expose
    lateinit var realBookInfo: BookInfo

    override val bookData: IBookData
        get() = getOrLoadBookInfo().bookData
    override val pages: IPagesInfo
        get() = getOrLoadBookInfo().pages


    private fun getOrLoadBookInfo(): BookInfo {
        if (!loaded) {
            realBookInfo = LibraryFactory.getMetadata(bookId).getBookInfoExtractor(bookId, EMPTY_CONTEXT_LOADER).bookInfo

            index.updateBook(realBookInfo)

            loaded = true
        }

        return realBookInfo
    }
}