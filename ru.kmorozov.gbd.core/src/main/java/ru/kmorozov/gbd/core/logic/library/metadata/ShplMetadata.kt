package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
class ShplMetadata private constructor() : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return bookId.contains(SHPL_BASE_URL)
    }

    override fun getExtractor(bookContext: BookContext): IImageExtractor {
        return ShplImageExtractor(bookContext)
    }

    override fun getBookExtractor(bookId: String): AbstractBookInfoExtractor {
        return ShplBookExtractor(bookId)
    }

    override fun needSetCookies(): Boolean {
        return false
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(ILibraryMetadata.APACHE_CONNECTOR)
    }

    companion object {

        const val SHPL_BASE_URL = "elib.shpl.ru"

        val SHPL_METADATA: ILibraryMetadata = ShplMetadata()
    }
}
