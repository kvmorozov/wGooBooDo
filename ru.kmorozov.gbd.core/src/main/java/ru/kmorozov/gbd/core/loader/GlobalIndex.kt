package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo

class GlobalIndex : IIndex {

    private constructor()

    override val books: List<IBookInfo>
        get() = TODO("Not yet implemented")
    override val bookIdsList: Set<String>
        get() = TODO("Not yet implemented")

    override fun getBookInfo(bookId: String): IBookInfo {
        TODO("Not yet implemented")
    }

    override fun updateIndex(books: List<IBookInfo>) {
        TODO("Not yet implemented")
    }

    override fun updateBook(book: IBookInfo) {

    }

    override fun updateContext() {
        TODO("Not yet implemented")
    }

    companion object {
        val INSTANCE = GlobalIndex()
    }
}