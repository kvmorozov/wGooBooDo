package ru.kmorozov.library.data.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.kmorozov.db.core.logic.model.book.BookInfo

/**
 * Created by km on 21.12.2016.
 */
interface GoogleBooksRepository : MongoRepository<BookInfo, String> {

    fun findByBookId(bookId: String): BookInfo?
}
