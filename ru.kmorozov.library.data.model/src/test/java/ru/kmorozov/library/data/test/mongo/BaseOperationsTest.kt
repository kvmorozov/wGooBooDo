package ru.kmorozov.library.data.test.mongo

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.kmorozov.library.data.config.MongoConfiguration
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.repository.BooksRepository
import ru.kmorozov.library.data.storage.mongo.LikeTextSearch
import java.util.*

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(MongoConfiguration::class))
class BaseOperationsTest {

    @Autowired
    internal lateinit var booksRepository: BooksRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun connectTest() {
        MatcherAssert.assertThat<BooksRepository>(booksRepository, notNullValue())
        booksRepository.deleteAll()
        MatcherAssert.assertThat(booksRepository.count(), `is`<Number>(0))
    }

    @Test
    fun crudTest() {
        val countBefore = booksRepository.count()

        val book = Book("Test title", "Test author")

        val savedBook = booksRepository.save(book)
        MatcherAssert.assertThat(savedBook, notNullValue())
        MatcherAssert.assertThat(savedBook, `is`(book))
        MatcherAssert.assertThat(booksRepository.count(), `is`(countBefore + 1L))
        booksRepository.delete(book)
        MatcherAssert.assertThat(booksRepository.count(), `is`(countBefore))
    }

    @Test
    fun searchTest() {
        val books = Arrays.asList(Book("Test tit1le", "Test aut1hor"), Book("Test tit2le", "Test aut2hor"))

        booksRepository.saveAll(books)

        try {
            val likeTextSearch = LikeTextSearch(Book::class.java.simpleName, mongoTemplate)
            MatcherAssert.assertThat(likeTextSearch.findMatchingIds("%aut1%").size, `is`(0))
            val criteria2 = TextCriteria.forDefaultLanguage().matching("Test")
            MatcherAssert.assertThat(booksRepository.findAllBy(criteria2).size, `is`(2))
        } finally {
            booksRepository.deleteAll(books)
        }
    }

    @Test
    fun findLinks() {
        val lnkBooks = booksRepository.streamByBookInfoFormat(BookInfo.BookFormat.LNK)
        Assert.assertTrue(10L > lnkBooks.count())
    }
}
