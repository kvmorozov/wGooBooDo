package ru.kmorozov.gbd.core.logic.model.book.base

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
interface IPagesInfo {

    val pages: Array<IPage>

    val missingPagesList: String

    fun build()

    fun getPageByPid(pid: String): IPage?
}
