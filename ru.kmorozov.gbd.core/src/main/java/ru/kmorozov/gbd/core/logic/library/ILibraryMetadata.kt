package ru.kmorozov.gbd.core.logic.library

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
interface ILibraryMetadata {

    fun isValidId(bookId: String): Boolean

    fun getExtractor(bookContext: BookContext): IImageExtractor

    fun getBookExtractor(bookId: String): AbstractBookExtractor

    fun getBookExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookExtractor

    fun needSetCookies(): Boolean

    fun preferredConnectors(): List<HttpConnector>
}
