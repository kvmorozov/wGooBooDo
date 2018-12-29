package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.IOException
import java.util.stream.Stream

interface IStorage {

    val isValidOrCreate: Boolean

    val bookIdsList: Set<String>

    val items: Stream<IStoredItem>?

    @Throws(IOException::class)
    fun getChildStorage(bookData: IBookData): IStorage?

    @Throws(IOException::class)
    fun size(): Int

    @Throws(IOException::class)
    fun isPageExists(page: IPage): Boolean

    @Throws(IOException::class)
    fun getStoredItem(page: IPage, imgFormat: String): IStoredItem?

    fun refresh()

    fun getIndex(indexName: String, createIfNotExists: Boolean): IIndex

    @Throws(IOException::class)
    fun restoreState(bookInfo: IBookInfo)
}
