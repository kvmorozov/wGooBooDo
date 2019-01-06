package ru.kmorozov.gbd.core.logic.extractors.archive

import org.jsoup.nodes.Document
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.core.logic.model.book.archive.ArchiveBookData
import ru.kmorozov.db.core.logic.model.book.archive.ArchivePage
import ru.kmorozov.db.core.logic.model.book.archive.ArchivePagesInfo
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.BASE_URL
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor

class ArchiveBookInfoExtractor(bookId: String) : AbstractBookInfoExtractor(bookId) {

    override val bookUrl: String
        get() = BASE_URL.replace(BOOK_ID_PLACEHOLDER, bookId)

    override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        val title = doc.head().children()[0].text().split(":")[0].trim()

        val pageElts = doc.body().getElementsByAttributeValue("itemprop", "numberOfPages")
        val numPages = if (pageElts.size == 0) 100 else pageElts[0].text().toInt()
        val params = doc.body().getElementById("theatre-controls").parent().children()[2].data().lines()[4].split("?")[1].split("&")

        val itemPath = params[1].split("=")[1]
        val server = params[2].split("=")[1]

        val pages = IntRange(1, numPages - 1).map { ArchivePage(bookId, it, itemPath, server) }

        val bookData = ArchiveBookData(title, bookId)

        return BookInfo(bookData, ArchivePagesInfo(pages.toTypedArray()), bookId)
    }
}