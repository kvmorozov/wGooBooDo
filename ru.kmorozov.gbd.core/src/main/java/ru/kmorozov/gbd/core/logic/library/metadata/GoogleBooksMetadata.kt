package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.asynchttp.AsyncHttpConnector
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector
import ru.kmorozov.gbd.core.logic.connectors.ok.OkHttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
class GoogleBooksMetadata private constructor() : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return 12 == bookId.length
    }

    override fun getExtractor(bookContext: BookContext): IImageExtractor {
        return GoogleImageExtractor(bookContext)
    }

    override fun getBookExtractor(bookId: String): AbstractBookExtractor {
        return GoogleBookInfoExtractor(bookId)
    }

    override fun getBookExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookExtractor {
        return GoogleBookInfoExtractor(bookId, storedLoader)
    }

    override fun needSetCookies(): Boolean {
        return true
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(GOOGLE_CONNECTOR, ASYNC_CONNECTOR, OK_CONNECTOR)
    }

    companion object {
        val GOOGLE_CONNECTOR = GoogleHttpConnector()
        val ASYNC_CONNECTOR = AsyncHttpConnector()
        val OK_CONNECTOR = OkHttpConnector()

        val GOOGLE_METADATA: ILibraryMetadata = GoogleBooksMetadata()
    }
}
