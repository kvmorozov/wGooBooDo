package ru.kmorozov.gbd.core.logic.extractors

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
open class SimplePageImgProcessor<T : AbstractPage> : AbstractPageImgProcessor<T> {

    constructor(bookContext: BookContext, page: T, usedProxy: HttpHostExt) : super(bookContext, page, usedProxy)

    protected override val successMsg: String
        get() = String.format("Finished img processing for %s", uniqueObject.pid)

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return String.format("Cannot load data from %s", imgUrl)
    }

    override fun run() {
        if (uniqueObject.isDataProcessed) return

        processImage(uniqueObject.imgUrl)
    }

    override fun validateOutput(storedItem: IStoredItem?, width: Int): Boolean {
        return true
    }
}