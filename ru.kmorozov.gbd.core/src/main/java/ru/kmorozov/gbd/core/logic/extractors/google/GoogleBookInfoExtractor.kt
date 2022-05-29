package ru.kmorozov.gbd.core.logic.extractors.google

import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.BookInfo.Companion.EMPTY_BOOK
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTP_TEMPLATE
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor

/**
 * Created by km on 08.10.2016.
 */
open class GoogleBookInfoExtractor : AbstractBookInfoExtractor {

    override val bookUrl: String
        get() = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL

    override val reserveBookUrl: String
        get() = HTTP_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookId) + OPEN_PAGE_ADD_URL

    constructor(bookId: String) : super(bookId, ContextProvider.contextProvider)

    constructor(bookId: String, storedLoader: IContextLoader) : super(bookId, storedLoader)

    override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return EMPTY_BOOK

        val scripts = doc.select("script")
        for (script in scripts) {
            val childs = script.childNodes()
            if (childs.isNotEmpty() && childs[0] is DataNode) {
                val data = (childs[0] as DataNode).wholeData

                if (data.isEmpty()) continue

                if (data.startsWith(ADD_FLAGS_ATTRIBUTE) && 0 < data.indexOf(OC_RUN_ATTRIBUTE)) {
                    val jsonStart = data.indexOf(OC_RUN_ATTRIBUTE) + OC_RUN_ATTRIBUTE.length + 1
                    val jsonEnd = data.lastIndexOf(BOOK_INFO_START_TAG) - 3

                    if (0 >= jsonStart || 0 >= jsonEnd) return EMPTY_BOOK

                    val pagesJsonData = data.substring(jsonStart, jsonEnd)
                    val pages = Mapper.gson.fromJson(pagesJsonData, GooglePagesInfo::class.java)

                    val endIndex1 = data.lastIndexOf(BOOK_INFO_END_TAG_1)
                    val endIndex2 = data.lastIndexOf(BOOK_INFO_END_TAG_2)
                    val endIndex: Int = if (endIndex1 > 0) endIndex1 else endIndex2
                    val bookJsonData = data.substring(data.indexOf(BOOK_INFO_START_TAG) - 2, endIndex - 3)
                    val bookData = Mapper.gson.fromJson(bookJsonData, GoogleBookData::class.java)

                    val result = BookInfo(bookData, pages, bookId)
                    result.pages.build()

                    return if (result.pages.pages.isNotEmpty())
                        result
                    else
                        EMPTY_BOOK
                }
            }
        }

        return EMPTY_BOOK
    }

    companion object {

        private const val ADD_FLAGS_ATTRIBUTE = "_OC_addFlags"
        private const val OC_RUN_ATTRIBUTE = "_OC_Run"
        private const val BOOK_INFO_START_TAG = "fullview"
        private const val BOOK_INFO_END_TAG_1 = "enableClips"
        private const val BOOK_INFO_END_TAG_2 = "enableUserFeedbackUI"
        private const val OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false"
    }
}
