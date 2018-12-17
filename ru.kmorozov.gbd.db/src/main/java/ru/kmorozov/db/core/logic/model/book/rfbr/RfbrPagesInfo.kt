package ru.kmorozov.db.core.logic.model.book.rfbr

import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage.Companion.EMPTY_RFBR_PAGE
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class RfbrPagesInfo(override val pages: Array<IPage>) : IPagesInfo {

    override val missingPagesList: String
        get() = ""

    override fun build() {

    }

    override fun getPageByPid(pid: String): IPage {
        return EMPTY_RFBR_PAGE
    }
}
