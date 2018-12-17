package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.apache.ApacheHttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import java.util.*

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

    override fun getBookExtractor(bookId: String): AbstractBookExtractor {
        return ShplBookExtractor(bookId)
    }

    override fun getBookExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookExtractor {
        return getBookExtractor(bookId)
    }

    override fun needSetCookies(): Boolean {
        return false
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return Arrays.asList<HttpConnector>(ApacheHttpConnector())
    }

    companion object {

        const val SHPL_BASE_URL = "elib.shpl.ru"

        val SHPL_METADATA: ILibraryMetadata = ShplMetadata()
    }
}
