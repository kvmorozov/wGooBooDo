package ru.kmorozov.db.core.config

import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
interface IContextLoader {

    val bookIdsList: Set<String>

    val contextSize: Int

    val isValid: Boolean

    val empty: Boolean

    fun updateIndex()

    fun updateContext()

    fun updateBookInfo(bookInfo: BookInfo)

    fun getBookInfo(bookId: String): IBookInfo

    fun refreshContext()
}
