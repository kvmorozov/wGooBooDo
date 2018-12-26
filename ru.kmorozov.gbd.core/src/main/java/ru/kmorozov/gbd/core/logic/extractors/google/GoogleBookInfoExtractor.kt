package ru.kmorozov.gbd.core.logic.extractors.google

import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTP_TEMPLATE
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor

/**
 * Created by km on 08.10.2016.
 */
open class GoogleBookInfoExtractor : AbstractBookExtractor {

    protected override val bookUrl: String
        get() = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL

    protected override val reserveBookUrl: String
        get() = HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL

    constructor(bookId: String) : super(bookId) {}

    constructor(bookId: String, storedLoader: IContextLoader) : super(bookId, storedLoader) {}

    @Throws(Exception::class)
    override fun findBookInfo(): BookInfo {
        val defaultDocument = documentWithoutProxy
        try {
            val defaultBookInfo = extractBookInfo(defaultDocument)
            if (null == defaultBookInfo) {
                HttpHostExt.NO_PROXY.forceInvalidate(false)

                for (proxy in AbstractProxyListProvider.INSTANCE.proxyList) {
                    val proxyBookInfo = extractBookInfo(getDocumentWithProxy(proxy))
                    if (null == proxyBookInfo)
                        proxy.forceInvalidate(true)
                    else
                        return proxyBookInfo
                }
            } else
                return defaultBookInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return BookInfo.EMPTY_BOOK
    }

    private fun extractBookInfo(doc: Document?): BookInfo? {
        if (null == doc) return null

        val scripts = doc.select("script")
        for (script in scripts) {
            val childs = script.childNodes()
            if (null != childs && !childs.isEmpty() && childs[0] is DataNode) {
                val data = (childs[0] as DataNode).wholeData

                if (null == data || data.isEmpty()) continue

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && 0 < data.indexOf(OC_RUN_ATTRIBUTE)) {
                    val jsonStart = data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length + 1
                    val jsonEnd = data.lastIndexOf(BOOK_INFO_START_TAG) - 3

                    if (0 >= jsonStart || 0 >= jsonEnd) return null

                    val pagesJsonData = data.substring(jsonStart, jsonEnd)
                    val pages = Mapper.gson.fromJson(pagesJsonData, GooglePagesInfo::class.java)

                    val bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, data.lastIndexOf(BOOK_INFO_END_TAG) - 3)
                    val bookData = Mapper.gson.fromJson(bookJsonData, GoogleBookData::class.java)

                    return BookInfo(bookData, pages, bookId)
                }
            }
        }

        return null
    }

    companion object {

        private const val ADD_FLAGS_ATTRIBUTE = "_OC_addFlags"
        private const val OC_RUN_ATTRIBUTE = "_OC_Run"
        private const val BOOK_INFO_START_TAG = "fullview"
        private const val BOOK_INFO_END_TAG = "enableUserFeedbackUI"
        private const val OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false"
    }
}
