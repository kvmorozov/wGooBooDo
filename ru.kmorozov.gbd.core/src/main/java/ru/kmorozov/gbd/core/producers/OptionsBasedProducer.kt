package ru.kmorozov.gbd.core.producers

import com.google.common.base.Strings
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import java.util.*

/**
 * Created by km on 12.11.2016.
 */
class OptionsBasedProducer : IBookListProducer {

    override var bookIds: Set<String> = HashSet()
        private set

    init {
        val bookId = GBDOptions.bookId

        if (!Strings.isNullOrEmpty(bookId))
            bookIds = bookId.split(";").filter { LibraryFactory.isValidId(it) }.toSet()
        else if (GBDOptions.isValidConfig)
            bookIds = ContextProvider.contextProvider.bookIdsList
    }
}
