package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

interface IIndex {

    val books: Array<IBookInfo>

    fun updateIndex(books: List<IBookInfo>)
}
