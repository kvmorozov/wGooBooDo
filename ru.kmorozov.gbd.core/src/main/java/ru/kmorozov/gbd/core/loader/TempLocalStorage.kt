package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.IOException

class TempLocalStorage private constructor() : LocalFSStorage(System.getProperty("java.io.tmpdir")) {

    @Throws(IOException::class)
    override fun getStoredItem(page: IPage, imgFormat: String): IStoredItem {
        return TempLocalItem(this, page, imgFormat)
    }

    companion object {

        val DEFAULT_TEMP_STORAGE: LocalFSStorage = TempLocalStorage()
    }
}
