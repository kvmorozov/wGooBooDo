package ru.kmorozov.db.core.config

import ru.kmorozov.db.core.logic.model.book.BookInfo

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
interface IContextLoader {

    val bookIdsList: Set<String>

    val contextSize: Int

    val isValid: Boolean

    fun updateIndex()

    fun updateContext()

    fun updateBookInfo(bookInfo: BookInfo)

    fun getBookInfo(bookId: String): BookInfo

    fun refreshContext()
}
