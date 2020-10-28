package ru.kmorozov.gbd.core.logic.extractors.google

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import java.util.stream.Collectors

/**
 * Created by km on 21.11.2015.
 */
class GooglePageImgProcessor(bookContext: BookContext, page: GooglePageInfo, usedProxy: HttpHostExt) : AbstractPageImgProcessor<GooglePageInfo>(bookContext, page, usedProxy) {

    private val urlBuilder: GoogleUrlBuilder

    protected override val successMsg: String
        get() = "Finished img processing for ${uniqueObject.pid}${if (uniqueObject.isGapPage) " with gap" else ""}"

    init {
        urlBuilder = GoogleUrlBuilder(bookContext.bookInfo.bookId)
    }

    private fun processImageWithProxy(proxy: HttpHostExt): Boolean {
        if (!proxy.isLocal && !proxy.isAvailable)
            return false

        return getImqRqUrls().stream().map { url -> processImage(url, proxy) }.findFirst().isPresent
    }

    override fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String {
        return String.format(IMG_ERROR_TEMPLATE, imgUrl, proxy.toString())
    }

    private fun preProcessPage() {
        val singlePageUrl = urlBuilder.getSinglePageUrl(uniqueObject.pid)
        val proxy = getSomeProxy()

        val doc = getDocumentWithProxy(singlePageUrl, getSomeProxy())
        if (doc.isPresent) {
            try {
                val sig = doc.get().getElementsByTag("script")[0].html()
                        .split("'")[3].split("\\x3d")[6].split("\\x26w")[0]

                if (!StringUtils.isEmpty(sig)) {
                    uniqueObject.sigs.add(sig)
                    usedProxy = proxy
                }
            } catch (ex: Exception) {
            }
        }
    }

    fun getImqRqUrls(): Set<String> {
        return uniqueObject.sigs.stream()
                .map { sig -> urlBuilder.getSigPageUrl(uniqueObject.pid, sig) }
                .collect(Collectors.toSet())
    }

    override fun run() {
        if (uniqueObject.isDataProcessed) return

//        preProcessPage()

        if (uniqueObject.sigs.isEmpty()) return

        if (!processImageWithProxy(usedProxy)) {
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (!usedProxy.isLocal && !GBDOptions.secureMode)
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
