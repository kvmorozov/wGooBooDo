package ru.kmorozov.gbd.core.logic.library

import ru.kmorozov.gbd.core.logic.library.metadata.ArchiveMetadata.Companion.ARCHIVE_METADATA
import ru.kmorozov.gbd.core.logic.library.metadata.GoogleBooksMetadata.Companion.GOOGLE_METADATA
import ru.kmorozov.gbd.core.logic.library.metadata.RfbrMetadata.Companion.RFBR_METADATA
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata.Companion.SHPL_METADATA
import ru.kmorozov.gbd.core.logic.library.metadata.UnknownMetadata.Companion.UNKNOWN_METADATA

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
object LibraryFactory {

    private val METADATA = arrayOf(GOOGLE_METADATA, SHPL_METADATA, RFBR_METADATA, ARCHIVE_METADATA, UNKNOWN_METADATA)

    fun getMetadata(bookId: String): ILibraryMetadata {
        return METADATA.first { it.isValidId(bookId) }
    }

    fun isValidId(bookId: String): Boolean {
        return !getMetadata(bookId).equals(UNKNOWN_METADATA)
    }
}
