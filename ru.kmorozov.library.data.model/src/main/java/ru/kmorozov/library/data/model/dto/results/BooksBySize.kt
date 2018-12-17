package ru.kmorozov.library.data.model.dto.results

import ru.kmorozov.library.data.model.book.BookInfo

/**
 * Created by sbt-morozov-kv on 28.07.2017.
 */
class BooksBySize {

    private var bookIds: List<String>? = null

    var count: Int? = null

    var size: Long? = null

    var format: BookInfo.BookFormat? = null

    fun getBookIds(): Collection<String>? {
        return bookIds
    }

    fun setBookIds(bookIds: List<String>) {
        this.bookIds = bookIds
    }
}
