package ru.kmorozov.db.core.logic.model.book

import com.google.gson.annotations.Expose
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo
import ru.kmorozov.gbd.logger.model.ILoggableObject

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
open class BookInfo : Serializable, ILoggableObject, IBookInfo {

    @Expose
    override val bookData: IBookData
    @Expose
    override val pages: IPagesInfo
    @Expose
    override var bookId: String

    constructor(bookData: IBookData, pages: IPagesInfo, bookId: String) {
        this.bookData = bookData
        this.pages = pages
        this.bookId = bookId
    }

    override val description: String?
        get() = bookData.title

    override var lastPdfChecked: Long = 0

    override val empty: Boolean
        get() = this == EMPTY_BOOK

    val locked: Boolean
        get() = pages.pages.map { it.isLoadingStarted }.contains(true)

    companion object {
        val EMPTY_BOOK: BookInfo = BookInfo(object : IBookData {
            override val title: String
                get() = "empty"
            override val volumeId: String
                get() = "empty"
        }, object : IPagesInfo {
            override val pages: Array<IPage>
                get() = arrayOf()
            override val missingPagesList: String
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override fun build() {

            }

            override fun getPageByPid(pid: String): IPage {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }, "")
    }
}
