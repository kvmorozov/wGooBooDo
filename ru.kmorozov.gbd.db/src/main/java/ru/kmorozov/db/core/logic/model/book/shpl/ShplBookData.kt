package ru.kmorozov.db.core.logic.model.book.shpl

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplBookData(title: String) : IBookData {

    override var title: String = ""
        get() = field.trim { it <= ' ' }

    override val volumeId: String
        get() = title.hashCode().toString()
}
