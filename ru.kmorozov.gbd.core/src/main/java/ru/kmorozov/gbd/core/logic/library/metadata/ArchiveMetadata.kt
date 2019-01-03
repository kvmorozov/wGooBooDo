package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.archive.ArchiveBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import java.util.regex.Pattern

class ArchiveMetadata private constructor() : ILibraryMetadata {

    val ARCHIVE_PATTERN = Pattern.compile("[a-z]*[0-9][0-9][a-z]*")

    override fun isValidId(bookId: String): Boolean {
        return ARCHIVE_PATTERN.matcher(bookId).matches()
    }

    override fun getExtractor(bookContext: BookContext): IImageExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        return ArchiveBookInfoExtractor(bookId)
    }

    override fun needSetCookies(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(ILibraryMetadata.APACHE_CONNECTOR)
    }

    companion object {
        val ARCHIVE_METADATA = ArchiveMetadata()
    }
}