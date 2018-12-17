package ru.kmorozov.library.data.repository

import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.book.Storage
import java.util.stream.Stream

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */
interface BooksRepository : MongoRepository<Book, String> {

    fun findAllBy(criteria: TextCriteria): Collection<Book>

    fun findAllByStorage(storage: Storage): List<Book>

    fun findAllByStorageAndBookInfoFormat(storage: Storage, bookInfo_format: BookInfo.BookFormat): List<Book>

    fun streamByBookInfoFormat(bookInfo_format: BookInfo.BookFormat): Stream<Book>

    fun findOneByBookInfoPath(path: String): Book?

    fun findAllByBookInfoFileName(fileName: String): List<Book>

    @Query("{ 'bookInfo.fileName' : { \$regex: ?0 } }")
    fun findBooksByRegexBookInfoFileName(regexp: String): List<Book>
}
