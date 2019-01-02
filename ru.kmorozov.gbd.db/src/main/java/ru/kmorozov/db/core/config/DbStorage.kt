package ru.kmorozov.db.core.config

import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.IOException
import java.util.stream.Stream

class DbStorage : IStorage {

    override val isValidOrCreate: Boolean
        get() = false

    override val bookIdsList: Set<String>
        get() = TODO("Not implemented yet")

    override val items: Stream<IStoredItem>?
        get() = null

    @Throws(IOException::class)
    override fun getChildStorage(bookData: IBookData): IStorage {
        TODO("Not implemented yet")
    }

    @Throws(IOException::class)
    override fun size(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun isPageExists(page: IPage): Boolean {
        return false
    }

    @Throws(IOException::class)
    override fun getStoredItem(page: IPage, imgFormat: String): IStoredItem {
        TODO("Not implemented yet")
    }

    override fun refresh() {

    }

    override fun getIndex(indexName: String, createIfNotExists: Boolean): IIndex {
        TODO("Not implemented yet")
    }

    override fun restoreState(bookInfo: IBookInfo) {

    }
}
