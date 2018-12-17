package ru.kmorozov.db.core.logic.model.book.rfbr

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

class RfbrPage(private val bookId: String, override val order: Int) : AbstractPage() {
    override fun compareTo(other: IPage): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private constructor() : this("", 0)

    override val pid: String
        get() = order.toString()

    override val imgUrl: String
        get() = String.format("http://www.rfbr.ru/rffi/djvu_page?objectId=%s&width=1000&page=%d", bookId, order)

    companion object {
        public val EMPTY_RFBR_PAGE: RfbrPage = RfbrPage()
    }
}

