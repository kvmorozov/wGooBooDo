package ru.kmorozov.library.data.loader

import ru.kmorozov.library.data.model.book.Book

import java.io.IOException

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
interface ILoader {

    @Throws(IOException::class)
    fun load()

    fun resolveLink(lnkBook: Book)

    fun downloadBook(book: Book)
}
