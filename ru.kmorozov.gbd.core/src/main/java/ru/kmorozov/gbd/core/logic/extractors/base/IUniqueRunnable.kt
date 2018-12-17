package ru.kmorozov.gbd.core.logic.extractors.base

/**
 * Created by km on 13.11.2016.
 */
interface IUniqueRunnable<T> : Runnable {

    var uniqueObject: T
}
