package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.File

open class MayBePageItem : ImageItem {

    var page: IPage? = null

    constructor(file: File) : super(file)

    constructor(file: File, page: IPage) : super(file) {
        upgrade(page)
    }

    open fun upgrade(page: IPage) {
        if (this.page == null) {
            this.page = page
            page.storedItem = this
        }
    }
}
