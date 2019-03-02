package ru.kmorozov.gbd.core.logic.extractors.google

import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_IMG_TEMPLATE
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor
import ru.kmorozov.gbd.utils.Images

/**
 * Created by km on 21.11.2015.
 */
internal class GooglePageImgProcessor(bookContext: BookContext, page: GooglePageInfo, usedProxy: HttpHostExt) : AbstractPageImgProcessor<GooglePageInfo>(bookContext, page, usedProxy) {

    protected override val successMsg: String
        get() = "Finished img processing for ${uniqueObject.pid}${if (uniqueObject.isGapPage) " with gap" else ""}"

    private fun processImageWithProxy(proxy: HttpHostExt): Boolean {
        return !(!proxy.isLocal && !proxy.isAvailable) && processImage(uniqueObject.getImqRqUrl(bookContext.bookInfo.bookId, HTTPS_IMG_TEMPLATE, imgWidth), proxy)
    }

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return String.format(IMG_ERROR_TEMPLATE, imgUrl, proxy.toString())
    }

    override fun run() {
        if (uniqueObject.isDataProcessed) return

        if (!processImageWithProxy(usedProxy)) {
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (!usedProxy.isLocal)
                if (!processImageWithProxy(HttpHostExt.NO_PROXY))
                // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
                    AbstractProxyListProvider.INSTANCE.parallelProxyStream.forEach { proxy ->
                        if (proxy !== usedProxy)
                            if (processImageWithProxy(proxy)) {
                            }
                    }
        }
    }

    override fun validateOutput(storedItem: IStoredItem?, width: Int): Boolean {
        return !Images.isInvalidImage(storedItem!!.asFile(), width)
    }

    companion object {

        private const val IMG_ERROR_TEMPLATE = "No img at %s with proxy %s"
    }
}
