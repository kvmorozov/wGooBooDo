package ru.kmorozov.gbd.core.producers

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory

import java.util.Collections
import java.util.HashSet

/**
 * Created by km on 12.11.2016.
 */
class OptionsBasedProducer : IBookListProducer {

    override var bookIds: Set<String> = HashSet()
        private set

    init {
        val bookId = GBDOptions.bookId

        if (!StringUtils.isEmpty(bookId) && LibraryFactory.isValidId(bookId))
            bookIds = HashSet(listOf(bookId))
        else if (GBDOptions.isValidConfig)
            bookIds = ContextProvider.contextProvider.bookIdsList
    }
}
