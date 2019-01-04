package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata.Companion.APACHE_GOOGLE_CONNECTOR
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata.Companion.ASYNC_CONNECTOR
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata.Companion.GOOGLE_CONNECTOR
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata.Companion.HTTP_2_CONNECTOR
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata.Companion.OK_CONNECTOR

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
class GoogleBooksMetadata private constructor() : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return 12 == bookId.length
    }

    override fun getImageExtractor(bookContext: BookContext): IImageExtractor {
        return GoogleImageExtractor(bookContext)
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        return GoogleBookInfoExtractor(bookId)
    }

    override fun getBookInfoExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookInfoExtractor {
        return GoogleBookInfoExtractor(bookId, storedLoader)
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(GOOGLE_CONNECTOR, HTTP_2_CONNECTOR, OK_CONNECTOR, ASYNC_CONNECTOR, APACHE_GOOGLE_CONNECTOR)
    }

    companion object {
        val GOOGLE_METADATA: ILibraryMetadata = GoogleBooksMetadata()
    }
}
