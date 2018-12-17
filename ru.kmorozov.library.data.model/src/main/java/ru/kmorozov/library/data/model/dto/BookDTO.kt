package ru.kmorozov.library.data.model.dto

import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat
import ru.kmorozov.library.utils.BookUtils

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
class BookDTO {

    var id: String? = null
    var format: BookFormat? = null
    var title: String? = null
    var path: String? = null
    var localPath: String? = null
    var size: String? = null
    var isLoaded: Boolean = false

    constructor() {}

    constructor(book: Book, loaded: Boolean) {
        this.id = book.bookId
        this.format = book.bookInfo.format
        this.title = if (null == book.title) book.bookInfo.fileName else book.title
        this.localPath = book.bookInfo.fileName
        this.isLoaded = loaded
        this.size = BookUtils.humanReadableByteCount(book.bookInfo.size, true)
    }
}
