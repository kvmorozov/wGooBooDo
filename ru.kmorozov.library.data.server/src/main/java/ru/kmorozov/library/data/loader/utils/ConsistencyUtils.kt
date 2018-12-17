package ru.kmorozov.library.data.loader.utils

import org.springframework.data.repository.CrudRepository
import ru.kmorozov.library.data.model.book.Book

import java.util.*

/**
 * Created by sbt-morozov-kv on 07.04.2017.
 */
object ConsistencyUtils {

    private const val DEDUPLCATION_ENABLED = false

    fun deduplicate(books: Collection<Book>, booksRepository: CrudRepository<Book, String>): Collection<Book> {
        if (!DEDUPLCATION_ENABLED)
            return books

        val uniquePaths = ArrayList<String>()
        val deduplicatedBooks = ArrayList<Book>()
        for (book in books)
            if (uniquePaths.add(book.bookInfo.path!!))
                deduplicatedBooks.add(book)
            else
                booksRepository.delete(book)

        return deduplicatedBooks
    }
}
