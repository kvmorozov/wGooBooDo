package ru.kmorozov.gbd.core.logic.model.book.base

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
interface IPagesInfo {

    val pages: Array<IPage>

    val missingPagesList: String
        get() = ""

    fun build() {}

    @Throws(PageNotFoundException::class)
    fun getPageByPid(pid: String): IPage
}
