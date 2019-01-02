package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.File

open class LocalFSStoredItem : RawFileItem {

    protected val storage: LocalFSStorage
    protected val page: IPage
    protected val imgFormat: String

    protected constructor(file: File, storage: LocalFSStorage, page: IPage, imgFormat: String) : super(file) {
        this.storage = storage
        this.page = page
        this.imgFormat = imgFormat
    }

    constructor(storage: LocalFSStorage, page: IPage, imgFormat: String) : this(File(storage.storageDir.path + File.separator + page.order + '_'.toString() + page.pid + '.'.toString() + imgFormat), storage, page, imgFormat) {}
}
