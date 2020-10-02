package ru.kmorozov.db.core.logic.model.book.shpl

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplBookData(private val bookId: String) : IBookData {

    override val volumeId: String
        get() = bookId

    override val title: String
        get() = bookId
}
