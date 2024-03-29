package ru.kmorozov.gbd.core.loader

import com.google.gson.annotations.Expose
import ru.kmorozov.db.core.config.EmptyContextLoader.Companion.EMPTY_CONTEXT_LOADER
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class LazyBookInfo(bookId: String, private val index: IIndex) :
    BookInfo(EMPTY_BOOK.bookData, EMPTY_BOOK.pages, bookId) {

    private var loaded: Boolean = false

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

            assert(!(realBookInfo is LazyBookInfo))

            index.updateBook(realBookInfo)

            loaded = true
        }

        return realBookInfo
    }
}