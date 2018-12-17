package ru.kmorozov.gbd.core.logic.library.metadata

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.apache.ApacheHttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

import java.util.Arrays

class RfbrMetadata : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return StringUtils.isNumeric(bookId)
    }

    override fun getExtractor(bookContext: BookContext): IImageExtractor {
        return RfbrImageExtractor(bookContext)
    }

    override fun getBookExtractor(bookId: String): AbstractBookExtractor {
        return RfbrBookExtractor(bookId)
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

        val RFBR_METADATA = RfbrMetadata()
    }
}
