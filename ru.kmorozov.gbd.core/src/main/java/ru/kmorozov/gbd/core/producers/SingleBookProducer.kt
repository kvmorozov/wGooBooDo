package ru.kmorozov.gbd.core.producers

import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import java.util.*

/**
 * Created by km on 12.11.2016.
 */
class SingleBookProducer(bookId: String) : IBookListProducer {

    override val bookIds: Set<String>

    init {
        bookIds = HashSet(listOf(bookId))
    }
}
