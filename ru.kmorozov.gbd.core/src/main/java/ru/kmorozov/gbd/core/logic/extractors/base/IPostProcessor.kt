package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.logic.context.BookContext

/**
 * Created by km on 09.11.2016.
 */
interface IPostProcessor : IUniqueRunnable<BookContext> {

    val withParam: Boolean
        get() = true

    fun make()
}
