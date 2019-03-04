package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

interface IIndex {

    val books: List<IBookInfo>

    val bookIdsList: Set<String>

    fun getBookInfo(bookId: String): IBookInfo

    fun updateIndex(books: List<IBookInfo>)

    fun updateBook(book: IBookInfo)

    fun updateContext()
}
