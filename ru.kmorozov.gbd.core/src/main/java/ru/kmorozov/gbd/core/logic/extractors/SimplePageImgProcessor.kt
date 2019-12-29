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
        get() = "Finished img processing for ${page.pid}"

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return "Cannot load data from $imgUrl"
    }

    override fun run() {
        if (page.isDataProcessed) return

        processImage(page.imgUrl)
    }

    override fun validateOutput(storedItem: IStoredItem, width: Int): Boolean {
        return true
    }
}
