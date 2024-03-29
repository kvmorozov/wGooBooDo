package ru.kmorozov.gbd.core.logic.extractors

import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
open class SimplePageImgProcessor<T : AbstractPage>(bookContext: BookContext, page: T, usedProxy: HttpHostExt) :
    AbstractPageImgProcessor<T>(bookContext, page, usedProxy) {

    override val successMsg: String
        get() = "Finished img processing for ${uniqueObject.pid}"

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return "Cannot load data from $imgUrl"
    }

    override fun run() {
        if (uniqueObject.isDataProcessed) return

        processImage(uniqueObject.imgUrl)
    }
}
