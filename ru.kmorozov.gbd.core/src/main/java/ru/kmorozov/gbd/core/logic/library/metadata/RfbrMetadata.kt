package ru.kmorozov.gbd.core.logic.library.metadata

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

class RfbrMetadata : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return StringUtils.isNumeric(bookId)
    }

    override fun getImageExtractor(bookContext: BookContext): IImageExtractor {
        return RfbrImageExtractor(bookContext)
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        return RfbrBookInfoExtractor(bookId)
    }

    override fun needSetCookies(): Boolean {
        return false
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return listOf(ILibraryMetadata.APACHE_CONNECTOR)
    }

    companion object {

        val RFBR_METADATA = RfbrMetadata()
    }
}
