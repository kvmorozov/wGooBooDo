package ru.kmorozov.db.core.logic.model.book.shpl

import com.google.gson.annotations.SerializedName
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage.Companion.EMPTY_SHPL_PAGE
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ShplPagesInfo(pages: Array<IPage>) : IPagesInfo {

    @field:SerializedName("page")
    public override val pages = pages

    override val missingPagesList: String
        get() = ""

    override fun build() {

    }

    override fun getPageByPid(pid: String): IPage {
        return EMPTY_SHPL_PAGE
    }
}
