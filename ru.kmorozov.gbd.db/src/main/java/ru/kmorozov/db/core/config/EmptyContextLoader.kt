package ru.kmorozov.db.core.config

import ru.kmorozov.db.core.logic.model.book.BookInfo

class EmptyContextLoader() : IContextLoader {
    override val empty: Boolean
        get() = true

    override val bookIdsList: Set<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val contextSize: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val isValid: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun updateContext() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookInfo(bookId: String): BookInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        val EMPTY_CONTEXT_LOADER : EmptyContextLoader = EmptyContextLoader()
    }
}