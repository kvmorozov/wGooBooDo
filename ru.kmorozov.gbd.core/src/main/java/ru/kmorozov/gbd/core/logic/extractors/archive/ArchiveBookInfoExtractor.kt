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

        val title = doc.body().children()[1].children()[5].children()[1].children()[3].allElements.eachText()[0]

        val numPages = Integer.valueOf(doc.body().children()[1].children()[5].children()[4].children()[0].children()[0].children()[0].children()[10].children()[9].allElements.eachText()[2])
        val params = doc.body().children()[1].children()[5].children()[1].children()[5].children()[0].children()[0].children()[2].data().lines()[4].split("?")[1].split("&")

        val itemPath = params[1].split("=")[1]
        val server = params[2].split("=")[1]

        val pages = IntRange(1, numPages).map { ArchivePage(bookId, it, itemPath, server) }

        val bookData = ArchiveBookData(title, bookId)

        return BookInfo(bookData, ArchivePagesInfo(pages.toTypedArray()), bookId)
    }
}