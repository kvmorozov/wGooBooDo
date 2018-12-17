package ru.kmorozov.gbd.core.logic.extractors.rfbr

import org.jsoup.nodes.Document
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrBookData
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPagesInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage

import java.util.Arrays

class RfbrBookExtractor(bookId: String) : AbstractBookExtractor(bookId) {

    protected override val bookUrl: String
        get() = RFBR_BASE_URL + bookId

    protected override val reserveBookUrl: String
        get() = bookUrl

    override fun findBookInfo(): BookInfo {
        var defaultDocument: Document? = null
        try {
            defaultDocument = documentWithoutProxy
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return extractBookInfo(defaultDocument)
    }

    private fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        val bookData = RfbrBookData(bookId)
        val numPages = Integer.valueOf(Arrays.stream(doc.html().split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).filter { s -> s.contains("readerInitialization") }.findAny().get().split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])

        val pages : MutableList<RfbrPage> = arrayListOf()

        for (index in 0 until numPages)
            pages[index] = RfbrPage(bookId, index)

        return BookInfo(bookData, RfbrPagesInfo(pages.toTypedArray()), bookId)
    }

    companion object {

        private const val RFBR_BASE_URL = "http://www.rfbr.ru/rffi/ru/books/o_"
    }
}
