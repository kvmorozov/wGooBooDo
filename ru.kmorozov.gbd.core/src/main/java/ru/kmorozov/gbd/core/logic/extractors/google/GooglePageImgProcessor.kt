package ru.kmorozov.gbd.core.logic.extractors.google

import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_IMG_TEMPLATE
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor
import ru.kmorozov.gbd.core.logic.proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import java.util.stream.Collectors

/**
 * Created by km on 21.11.2015.
 */
internal class GooglePageImgProcessor(bookContext: BookContext, page: GooglePageInfo, usedProxy: HttpHostExt) : AbstractPageImgProcessor<GooglePageInfo>(bookContext, page, usedProxy) {

    protected override val successMsg: String
        get() = "Finished img processing for ${uniqueObject.pid}${if (uniqueObject.isGapPage) " with gap" else ""}"

    private fun processImageWithProxy(proxy: HttpHostExt): Boolean {
        if (!proxy.isLocal && !proxy.isAvailable)
            return false

        return getImqRqUrls().stream().map { url -> processImage(url, proxy) }.findFirst().isPresent
    }

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return String.format(IMG_ERROR_TEMPLATE, imgUrl, proxy.toString())
    }

    fun getImqRqUrls(): Set<String> {
        val bookId = bookContext.bookInfo.bookId
        val urlTemplate = HTTPS_IMG_TEMPLATE
        val width = if (0 == GBDOptions.imageWidth) GoogleConstants.DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        return uniqueObject.sigs.stream().map { sig ->
                    urlTemplate.replace(GoogleConstants.BOOK_ID_PLACEHOLDER, bookId) +
                            GoogleConstants.IMG_REQUEST_TEMPLATE.replace(GoogleConstants.RQ_PG_PLACEHOLDER, uniqueObject.pid)
                                    .replace(GoogleConstants.RQ_SIG_PLACEHOLDER, sig!!)
                                    .replace(GoogleConstants.RQ_WIDTH_PLACEHOLDER, width.toString())
                }
                .collect(Collectors.toSet())
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
                            processImageWithProxy(proxy)
                    }
        }
    }

    companion object {

        private const val IMG_ERROR_TEMPLATE = "No img at %s with proxy %s"
    }
}
