package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.File

open class LocalFSStoredItem protected constructor(file: File, protected val storage: LocalFSStorage, protected val page: IPage, protected val imgFormat: String) : RawFileItem(file) {

    constructor(storage: LocalFSStorage, page: IPage, imgFormat: String) : this(File(storage.storageDir.path + File.separator + page.order + '_'.toString() + page.pid + '.'.toString() + imgFormat), storage, page, imgFormat) {}
}
