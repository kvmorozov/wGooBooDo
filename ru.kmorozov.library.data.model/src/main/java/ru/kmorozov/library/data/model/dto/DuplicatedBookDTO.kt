package ru.kmorozov.library.data.model.dto

import ru.kmorozov.library.data.model.dto.results.BooksBySize

/**
 * Created by sbt-morozov-kv on 27.07.2017.
 */
class DuplicatedBookDTO {

    var books: List<BookDTO>? = null

    private var count: Int? = 0

    private var size: Long? = 0

    private var format: String = "unknown"

    constructor()

    constructor(duplicatedBook: BooksBySize) {
        this.count = duplicatedBook.count
        this.format = duplicatedBook.format!!.name
        this.size = duplicatedBook.size
    }
}
