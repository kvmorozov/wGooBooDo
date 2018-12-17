package ru.kmorozov.gbd.core.logic.model.book.base

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
interface IPage : Comparable<IPage> {

    val pid: String

    val order: Int

    val isSigChecked: Boolean

    val isDataProcessed: Boolean

    val isFileExists: Boolean

    val isLoadingStarted: Boolean
}
