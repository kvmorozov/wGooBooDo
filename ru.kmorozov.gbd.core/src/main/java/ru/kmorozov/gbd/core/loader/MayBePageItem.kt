package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.io.File

open class MayBePageItem : ImageItem {

    var page: IPage = DUMMY_PAGE

    constructor(file: File) : super(file)

    public var prev: MayBePageItem? = null
    public var next: MayBePageItem? = null

    constructor(file: File, page: IPage) : super(file) {
        upgrade(page)
    }

    open fun upgrade(page: IPage) {
        if (this.page == DUMMY_PAGE) {
            this.page = page
            page.storedItem = this
        }
    }

    override val pageNum: Int
        get() = if (page == DUMMY_PAGE) super.pageNum else page.order

    companion object DUMMY_PAGE : IPage {
        override val pid: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val order: Int
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val isDataProcessed: Boolean
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val isFileExists: Boolean
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val isLoadingStarted: Boolean
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override var isScanned: Boolean
            get() = true
            set(value) {}
        override var storedItem: IStoredItem
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
            set(value) {}
    }
}
