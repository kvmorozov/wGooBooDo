package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class LazyBookInfo(override val bookId: String) : IBookInfo {
    override val empty: Boolean
        get() = true

    var realBookInfo: BookInfo? = null
        get() = LibraryFactory.getMetadata(bookId).getBookInfoExtractor(bookId).bookInfo

    override val bookData: IBookData
        get() = realBookInfo!!.bookData
    override val pages: IPagesInfo
        get() = realBookInfo!!.pages


}