package ru.kmorozov.db.core.logic.model.book.archive

import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

class ArchivePagesInfo(override val pages: Array<IPage>) : IPagesInfo {

    override fun getPageByPid(pid: String): IPage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}