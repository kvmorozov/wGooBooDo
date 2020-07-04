package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream

/**
 * Created by km on 08.11.2016.
 */
class BookContext {
    val postProcessor: IPostProcessor

    internal constructor(bookId: String, postProcessor: IPostProcessor) {
        this.postProcessor = postProcessor
        this.metadata = LibraryFactory.getMetadata(bookId)
        this.bookInfo = metadata.getBookInfoExtractor(bookId).bookInfo
        pagesBefore = pagesStream.filter { pageInfo -> pageInfo.isFileExists }.count()
        sigExecutor = QueuedThreadPoolExecutor(1, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { true }, "Sig_$bookId")
        imgExecutor = QueuedThreadPoolExecutor(0, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { x -> x.isDataProcessed }, "Img_$bookId")
        extractor = metadata.getImageExtractor(this)
        logger = ExecutionContext.INSTANCE.getLogger(BookContext::class.java, this)

        prepareStorage()
    }

    val sigExecutor: QueuedThreadPoolExecutor<out AbstractHttpProcessor>
    val imgExecutor: QueuedThreadPoolExecutor<AbstractPage>
    var bookInfo: IBookInfo
    private val metadata: ILibraryMetadata
    var started: AtomicBoolean = AtomicBoolean(false)
    var pdfCompleted: AtomicBoolean = AtomicBoolean(true)
    lateinit var storage: IStorage
    var extractor: IImageExtractor
    var pagesBefore: Long = 0
    var pagesProcessed: AtomicLong = AtomicLong(0)
    private val logger: Logger

    protected fun prepareStorage() {
        if (!GBDOptions.storage.isValidOrCreate || bookInfo == BookInfo.EMPTY_BOOK) return

        logger.info(if (ExecutionContext.INSTANCE.isSingleMode) "Working with ${bookInfo.bookData.title}" else "Starting...")

        storage = GBDOptions.storage.getChildStorage(bookInfo.bookData)

        if (!storage.isValidOrCreate)
            logger.severe("Invalid book title: ${bookInfo.bookData.title}")
    }

    val bookId: String
        get() = bookInfo.bookId

    val isPdfCompleted: Boolean
        get() = pdfCompleted.get()

    val isImgStarted: Boolean
        get() = started.get()

    val pagesStream: Stream<IPage>
        get() = Arrays.stream(bookInfo.pages.pages).sorted()

    fun getPostProcessor(): Runnable {
        postProcessor.uniqueObject = this
        return postProcessor
    }

    override fun toString(): String {
        return bookInfo.bookId + ' '.toString() + bookInfo.bookData.title
    }

    fun restoreState() {
        try {
            pagesStream.filter { it.isFileExists }.forEach { page ->
                try {
                    if (!storage.isPageExists(page)) {
                        logger.severe("Page ${page.pid} not found in storage!")
                        (page as AbstractPage).isDataProcessed = false
                        page.isFileExists = false
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            storage.restoreState(bookInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pagesBefore = pagesStream.filter { it.isFileExists }.count()
    }
}
