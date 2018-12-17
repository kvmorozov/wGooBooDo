package ru.kmorozov.gbd.core.logic.context

/**
 * Created by km on 12.11.2016.
 */
@FunctionalInterface
interface IBookListProducer {

    val bookIds: Set<String>
}
