package ru.kmorozov.db.core.logic.model.book.rfbr

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPagesInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

class RfbrPagesInfo(override val pages: Array<IPage>) : AbstractPagesInfo() {

    override fun getPageByPid(pid: String): IPage {
        TODO("not implemented yet")
    }
}
