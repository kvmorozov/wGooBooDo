package ru.kmorozov.gbd.core.logic.extractors.archive

import org.jsoup.nodes.Document
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.BASE_URL
import ru.kmorozov.gbd.core.config.constants.ArchiveConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor

class ArchiveBookInfoExtractor(bookId: String) : AbstractBookInfoExtractor(bookId) {

    override val bookUrl: String
        get() = BASE_URL.replace(BOOK_ID_PLACEHOLDER, bookId)

    override fun extractBookInfo(doc: Document?): BookInfo {
        if (null == doc) return BookInfo.EMPTY_BOOK

        return BookInfo.EMPTY_BOOK
    }
}