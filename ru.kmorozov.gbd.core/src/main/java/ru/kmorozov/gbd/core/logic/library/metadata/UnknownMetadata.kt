package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

class UnknownMetadata : ILibraryMetadata {

    private constructor()

    override fun isValidId(bookId: String): Boolean {
        return true
    }

    override fun getExtractor(bookContext: BookContext): IImageExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookExtractor(bookId: String): AbstractBookExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun needSetCookies(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun preferredConnectors(): List<HttpConnector> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        val UNKNOWN_METADATA: UnknownMetadata = UnknownMetadata()
    }

}