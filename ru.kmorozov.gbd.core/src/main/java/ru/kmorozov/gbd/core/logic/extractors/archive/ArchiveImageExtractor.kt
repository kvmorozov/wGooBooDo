package ru.kmorozov.gbd.core.logic.extractors.archive

import ru.kmorozov.db.core.logic.model.book.archive.ArchivePage
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor

class ArchiveImageExtractor(bookContext: BookContext) : AbstractImageExtractor<ArchivePage>(bookContext, ArchiveImageExtractor::class.java) {

}
