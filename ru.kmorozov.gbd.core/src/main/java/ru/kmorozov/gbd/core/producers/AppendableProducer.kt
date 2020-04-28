package ru.kmorozov.gbd.core.producers

import ru.kmorozov.gbd.core.logic.context.IBookListProducer

class AppendableProducer : IBookListProducer {

    private val internalBookIds : MutableSet<String> = HashSet<String>()

    override val bookIds: Set<String>
        get() = internalBookIds

    fun appendBook(bookId: String) {
        internalBookIds.add(bookId)
    }
}