package ru.kmorozov.gbd.core.logic.library.metadata

import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata

class UnknownMetadata private constructor() : ILibraryMetadata {

    override fun isValidId(bookId: String): Boolean {
        return true
    }

    override fun getImageExtractor(bookContext: BookContext): IImageExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookInfoExtractor(bookId: String): AbstractBookInfoExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookInfoExtractor(bookId: String, storedLoader: IContextLoader): AbstractBookInfoExtractor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun preferredConnectors(): List<HttpConnector> {
        return emptyList()
    }

    companion object {
        val UNKNOWN_METADATA: UnknownMetadata = UnknownMetadata()
    }

}