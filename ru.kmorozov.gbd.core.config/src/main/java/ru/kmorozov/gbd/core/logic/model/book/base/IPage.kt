package ru.kmorozov.gbd.core.logic.model.book.base

import ru.kmorozov.gbd.core.config.IStoredItem

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
interface IPage : Comparable<IPage> {

    val pid: String

    var order: Int

    val isDataProcessed: Boolean

    val isFileExists: Boolean

    val isLoadingStarted: Boolean

    var isScanned: Boolean

    var storedItem: IStoredItem

    override fun compareTo(other: IPage): Int {
        return order.compareTo(other.order)
    }
}
