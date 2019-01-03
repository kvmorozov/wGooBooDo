package ru.kmorozov.gbd.core.logic.extractors.rfbr

import org.jsoup.nodes.Document
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrBookData
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPagesInfo

import java.util.Arrays
import java.util.stream.IntStream

class RfbrBookInfoExtractor(bookId: String) : AbstractBookInfoExtractor(bookId) {

    protected override val bookUrl: String
        get() = RFBR_BASE_URL + bookId

    protected override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        val bookData = RfbrBookData(bookId)
        val numPages = Integer.valueOf(Arrays.stream(doc.html().split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).filter { s -> s.contains("readerInitialization") }.findAny().get().split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])

        val pages = IntRange(0, numPages).map {RfbrPage(bookId, it)}

        return BookInfo(bookData, RfbrPagesInfo(pages.toTypedArray()), bookId)
    }

    companion object {

        private const val RFBR_BASE_URL = "http://www.rfbr.ru/rffi/ru/books/o_"
    }
}
