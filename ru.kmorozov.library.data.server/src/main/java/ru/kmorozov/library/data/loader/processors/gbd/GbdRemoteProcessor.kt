package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.output.DummyReceiver
import ru.kmorozov.library.data.loader.processors.IGbdProcessor
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.IdType
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.repository.BooksRepository
import ru.kmorozov.library.data.repository.StorageRepository
import ru.kmorozov.library.data.server.options.ServerGBDOptions
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider

import java.io.IOException
import java.util.Optional

import ru.kmorozov.library.data.model.book.BookInfo.BookFormat.PDF
import ru.kmorozov.library.data.model.book.BookInfo.BookType.GOOGLE_BOOK

@Component
@ComponentScan(basePackageClasses = arrayOf(OneDriveContextLoader::class, ServerProducer::class, DbContextLoader::class, ServerGBDOptions::class))
@Conditional(StorageEnabledCondition::class)
class GbdRemoteProcessor : IGbdProcessor {

    @Autowired
    @Lazy
    private val api: OneDriveProvider? = null

    @Autowired
    @Lazy
    private val storageRepository: StorageRepository? = null

    @Autowired
    @Lazy
    private val booksRepository: BooksRepository? = null

    @Autowired
    @Lazy
    private val ctx: OneDriveContextLoader? = null

    @Autowired
    @Lazy
    private val producer: ServerProducer? = null

    @Autowired
    @Lazy
    private lateinit var dbCtx: DbContextLoader

    @Autowired
    @Qualifier("remote")
    private val options: ServerGBDOptions? = null

    val gbdRoot: OneDriveItem?
        @Bean
        @Lazy
        get() {
            try {
                val searchResults = api!!.search("books.ctx")
                if (searchResults.size == 1)
                    return searchResults[0].parent
                else
                    logger.error("Cannot find GBD root!")
            } catch (e: IOException) {
                logger.error("Search error", e)
            }

            return null
        }

    override fun process() {
        logger.info("Process GBD started.")

        val gbdRoot = gbdRoot
        if (gbdRoot == null) {
            logger.error("GBD root not found, exiting.")
            return
        }

        ExecutionContext.initContext(DummyReceiver(), true)

        ctx!!.initContext(gbdRoot)
        if (!ctx.bookIdsList.isEmpty())
            for (bookId in ctx.bookIdsList) {
                val bookInfo = ctx.getBookInfo(bookId)
                if (bookInfo != null) {
                    val dirItem = ctx.getBookDir(bookId)
                    val storage = storageRepository!!.findByUrl(dirItem.id!!)
                    val opBook = booksRepository!!.findAllByStorage(storage!!)
                            .stream().filter { book -> book.bookInfo.format === PDF }.findFirst()
                    if (opBook.isPresent) {
                        val book = opBook.get()
                        book.addBookId(IdType.GOOGLE_BOOK_ID, bookId)
                        book.bookInfo.bookType = GOOGLE_BOOK

                        booksRepository.save(book)

                        ctx.updateBookInfo(bookInfo)

                        logger.info(String.format("BookId %s processed.", bookId))
                    }
                }
            }

        logger.info("Process GBD finished.")
    }

    override fun load(bookId: String) {
        options!!.bookId = bookId
        GBDOptions.init(options)

        ContextProvider.setDefaultContextProvider(dbCtx)

        ExecutionContext.initContext(DummyReceiver(), 1 == producer!!.bookIds.size)
        ExecutionContext.INSTANCE.addBookContext(producer, DummyProgress(), ServerPdfMaker())

        ExecutionContext.INSTANCE.execute()
    }

    companion object {

        protected val logger = Logger.getLogger(GbdRemoteProcessor::class.java)
    }
}