package ru.kmorozov.library.data.loader.processors

import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOptions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.dto.BookDTO
import ru.kmorozov.library.data.model.dto.DuplicatedBookDTO
import ru.kmorozov.library.data.model.dto.results.BooksBySize
import ru.kmorozov.library.data.repository.BooksRepository
import ru.kmorozov.library.utils.BookUtils
import ru.kmorozov.onedrive.client.OneDriveProvider
import java.io.IOException
import java.util.stream.Collectors

@Component
class DuplicatesProcessor : IProcessor {

    @Autowired
    private val mongoTemplate: MongoTemplate? = null

    @Autowired
    protected var booksRepository: BooksRepository? = null

    @Autowired
    @Lazy
    private val api: OneDriveProvider? = null

    private val duplicates: List<BooksBySize>
        get() {
            val booksAggregation = Aggregation.newAggregation(Book::class.java,
                    Aggregation.group("bookInfo.size", "bookInfo.format")
                            .addToSet("bookId").`as`("bookIds")
                            .count().`as`("count"),
                    Aggregation.match(Criteria.where("count").gt(1.0)),
                    Aggregation.project("bookIds", "count", "size", "format"),
                    Aggregation.skip(0L)
            ).withOptions(AggregationOptions(true, false, Document("batchSize", 1000.0)))

            val results = mongoTemplate!!.aggregate(booksAggregation, BooksBySize::class.java)

            return results.mappedResults
        }

    fun findDuplicates(): List<DuplicatedBookDTO> {
        return duplicates.stream().map<DuplicatedBookDTO> { this.createDuplicateDTO(it) }.collect(Collectors.toList())
    }

    override fun process() {
        logger.info("Process duplicates started.")

        loop@ for (duplicate in duplicates)
            when (duplicate.format) {
                BookInfo.BookFormat.DJVU, BookInfo.BookFormat.PDF -> {
                    val books = duplicate.getBookIds()!!
                            .stream()
                            .map { id -> booksRepository!!.findById(id) }
                            .filter { it.isPresent() }
                            .map<Book> { it.get() }
                            .collect(Collectors.toList())

                    if (books.size < 2)
                        continue@loop

                    val mainBook = books[0]

                    for (book in books)
                        BookUtils.mergeCategories(book, mainBook)

                    booksRepository!!.save(mainBook)

                    for (book in books)
                        if (book === mainBook)
                            continue
                        else {
                            booksRepository!!.delete(book)
                            try {
                                val bookItem = api!!.getItem(book.bookInfo.path!!)
                                api.delete(bookItem)
                            } catch (e: IOException) {
                                logger.error(String.format("Failed delete OneDriveItem for %s : %s", mainBook.bookInfo.fileName, e.message))
                            }

                        }

                    logger.info(String.format("Duplicates for %s processed.", mainBook.bookInfo.fileName))
                }
            }

        logger.info("Process duplicates finished.")
    }

    private fun createDuplicateDTO(book: BooksBySize): DuplicatedBookDTO {
        val dto = DuplicatedBookDTO(book)
        dto.books = book.getBookIds()!!.stream().map { id -> BookDTO(booksRepository!!.findById(id).get(), false) }.collect(Collectors.toList())

        return dto
    }

    companion object {

        protected val logger = Logger.getLogger(DuplicatesProcessor::class.java)
    }
}
