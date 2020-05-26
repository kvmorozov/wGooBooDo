package ru.kmorozov.gbd.core.logic.extractors.base

interface IUniqueReusable<T : Any> : IUniqueRunnable<T> {

    var reuseCallback: (IUniqueReusable<T>) -> Unit

    fun initReusable(pattern: IUniqueReusable<T>) : Boolean
}