package ru.kmorozov.gbd.core.logic.model.book.base

interface IBookInfo {

    val bookData: IBookData

    val pages: IPagesInfo

    val bookId: String
}
