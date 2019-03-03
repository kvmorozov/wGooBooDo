package ru.kmorozov.gbd.core.logic.model.book.base

interface IBookInfo {

    var lastPdfChecked: Long

    val bookData: IBookData

    val pages: IPagesInfo

    val bookId: String

    val empty: Boolean
}
