package ru.kmorozov.db.core.logic.model.book.rfbr

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData

class RfbrBookData(private val bookId: String) : IBookData {

    override val volumeId: String?
        get() = bookId

    override val title: String?
        get() = bookId
}
