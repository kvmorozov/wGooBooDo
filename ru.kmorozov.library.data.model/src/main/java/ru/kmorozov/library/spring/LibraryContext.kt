package ru.kmorozov.library.spring

import org.springframework.context.support.StaticApplicationContext
import ru.kmorozov.library.data.repository.BooksRepository

/**
 * Created by sbt-morozov-kv on 15.12.2016.
 */
class LibraryContext private constructor() : StaticApplicationContext() {

    init {
        registerSingleton("booksRepository", BooksRepository::class.java)
    }

    companion object {

        val LIBRARY_CONTEXT = LibraryContext()
    }
}
