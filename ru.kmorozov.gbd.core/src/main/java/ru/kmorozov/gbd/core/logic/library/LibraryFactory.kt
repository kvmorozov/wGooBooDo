package ru.kmorozov.gbd.core.logic.library

import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.library.metadata.GoogleBooksMetadata
import ru.kmorozov.gbd.core.logic.library.metadata.RfbrMetadata
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata
import ru.kmorozov.gbd.core.logic.library.metadata.UnknownMetadata.Companion.UNKNOWN_METADATA

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
object LibraryFactory {

    private val METADATA = arrayOf(GoogleBooksMetadata.GOOGLE_METADATA, ShplMetadata.SHPL_METADATA, RfbrMetadata.RFBR_METADATA)

    private var selectedMetadata: ILibraryMetadata? = null

    fun getMetadata(bookId: String): ILibraryMetadata {
        for (_metadata in METADATA)
            if (_metadata.isValidId(bookId)) {
                selectedMetadata = _metadata
                return _metadata
            }

        return UNKNOWN_METADATA
    }

    fun isValidId(bookId: String): Boolean {
        return !getMetadata(bookId).equals(UNKNOWN_METADATA)
    }

    fun needSetCookies(): Boolean {
        return selectedMetadata!!.needSetCookies()
    }

    fun preferredConnectors(): List<HttpConnector> {
        return selectedMetadata!!.preferredConnectors()
    }
}
