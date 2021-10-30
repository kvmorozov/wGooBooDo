package ru.kmorozov.db.core.logic.model.book.shpl

import com.google.gson.annotations.SerializedName
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage.Companion.EMPTY_SHPL_PAGE
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPagesInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplPagesInfo(pages: Array<IPage>) : AbstractPagesInfo() {

    @field:SerializedName("page")
    override val pages = pages

    override fun getPageByPid(pid: String): IPage {
        return EMPTY_SHPL_PAGE
    }
}
