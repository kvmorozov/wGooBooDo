package ru.kmorozov.gbd.core.logic.library

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector
import ru.kmorozov.gbd.core.logic.connectors.http2native.Http2Connector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
interface ILibraryMetadata {

    fun isValidId(bookId: String): Boolean

    fun getImageExtractor(bookContext: BookContext): IImageExtractor

    fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor

    fun getBookInfoExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookInfoExtractor {
        return getBookInfoExtractor(bookId)
    }

    fun preferredConnectors(): List<HttpConnector>

    companion object {
        val GOOGLE_CONNECTOR = GoogleHttpConnector()
        val HTTP_2_CONNECTOR = Http2Connector()
    }
}
