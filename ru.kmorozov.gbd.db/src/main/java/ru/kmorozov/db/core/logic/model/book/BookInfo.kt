package ru.kmorozov.db.core.logic.model.book

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo
import ru.kmorozov.gbd.logger.model.ILoggableObject

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
class BookInfo : Serializable, ILoggableObject, IBookInfo {

    override val bookData: IBookData

    override val pages: IPagesInfo

    override val bookId: String

    override val description: String?
        get() = bookData.title

    var lastPdfChecked: Long = 0

    constructor(bookData: IBookData, pages: IPagesInfo, bookId: String) {
        this.bookData = bookData
        this.pages = pages
        this.bookId = bookId
    }

    override val empty: Boolean
        get() = this == EMPTY_BOOK

    companion object {
        val EMPTY_BOOK: BookInfo = BookInfo(object : IBookData {
            override val title: String
                get() = "empty"
            override val volumeId: String
                get() = "empty"
        }, object : IPagesInfo {
            override val pages: Array<IPage>
                get() = arrayOf<IPage>()
            override val missingPagesList: String
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override fun build() {

            }

            override fun getPageByPid(pid: String): IPage? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }, "")
    }
}
