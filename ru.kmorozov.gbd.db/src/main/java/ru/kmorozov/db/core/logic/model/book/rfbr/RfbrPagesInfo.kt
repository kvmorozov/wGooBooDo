package ru.kmorozov.db.core.logic.model.book.rfbr

import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class RfbrPagesInfo(override val pages: Array<IPage>) : IPagesInfo {

    override fun getPageByPid(pid: String): IPage {
        TODO("not implemented yet")
    }
}
