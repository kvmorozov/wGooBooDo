package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
class ShplMetadata private constructor() : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return bookId.contains(SHPL_BASE_URL)
    }

    override fun getImageExtractor(bookContext: BookContext): IImageExtractor {
        return ShplImageExtractor(bookContext)
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        return ShplBookInfoExtractor(bookId)
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(ILibraryMetadata.HTTP_2_CONNECTOR)
    }

    companion object {

        const val SHPL_BASE_URL = "elib.shpl.ru"

        val SHPL_METADATA: ILibraryMetadata = ShplMetadata()
    }
}
