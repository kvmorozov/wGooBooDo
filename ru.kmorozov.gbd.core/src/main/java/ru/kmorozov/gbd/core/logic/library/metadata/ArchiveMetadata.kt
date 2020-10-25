package ru.kmorozov.gbd.core.logic.library.metadata

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.apache.ApacheHttpConnector
import ru.kmorozov.gbd.core.logic.connectors.apache.ArchiveApacheConnections
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.archive.ArchiveBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.archive.ArchiveImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import java.util.regex.Pattern

class ArchiveMetadata private constructor() : ILibraryMetadata {

    val ARCHIVE_PATTERN = Pattern.compile("[a-z]*[0-9]*[a-z]*")

    override fun isValidId(bookId: String): Boolean {
        return !StringUtils.isEmpty(bookId) && ARCHIVE_PATTERN.matcher(bookId).matches()
    }

    override fun getImageExtractor(bookContext: BookContext): IImageExtractor {
        return ArchiveImageExtractor(bookContext)
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        return ArchiveBookInfoExtractor(bookId)
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(ApacheHttpConnector(ArchiveApacheConnections.INSTANCE))
    }

    companion object {
        val ARCHIVE_METADATA = ArchiveMetadata()
    }
}