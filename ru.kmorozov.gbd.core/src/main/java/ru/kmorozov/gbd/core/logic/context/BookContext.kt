package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.progress.IProgress
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream

/**
 * Created by km on 08.11.2016.
 */
class BookContext {
    val progress: IProgress
    val postProcessor: IPostProcessor

    internal constructor(bookId: String, progress: IProgress, postProcessor: IPostProcessor) {
        this.progress = progress
        this.postProcessor = postProcessor
        this.started = AtomicBoolean(false)
        this.pdfCompleted = AtomicBoolean(false)
        this.metadata = LibraryFactory.getMetadata(bookId)
        this.bookInfo = metadata.getBookExtractor(bookId).bookInfo
        pagesBefore = pagesStream.filter { pageInfo -> pageInfo.isFileExists }.count()
        sigExecutor = QueuedThreadPoolExecutor(1L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { true }, "Sig_$bookId")
        imgExecutor = QueuedThreadPoolExecutor(0L, QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { x -> x.isDataProcessed }, "Img_$bookId")
        extractor = metadata.getExtractor(this)
        logger = ExecutionContext.INSTANCE.getLogger(BookContext::class.java, this)
    }

    val sigExecutor: QueuedThreadPoolExecutor<out AbstractHttpProcessor>
    val imgExecutor: QueuedThreadPoolExecutor<AbstractPage>
    var bookInfo: BookInfo
    private val metadata: ILibraryMetadata
    var started: AtomicBoolean
    var pdfCompleted: AtomicBoolean
    var storage: IStorage? = null
    var extractor: IImageExtractor
    var pagesBefore: Long = 0
    var pagesProcessed: Long = 0
    private val logger: Logger

    val bookId: String
        get() = bookInfo.bookId

    val isPdfCompleted: Boolean
        get() = pdfCompleted.get()

    val isImgStarted: Boolean
        get() = started.get()

    val pagesStream: Stream<IPage>
        get() = Arrays.stream(bookInfo.pages.pages)

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
                    if (!storage!!.isPageExists(page)) {
                        logger.severe(String.format("Page %s not found in storage!", page.pid))
                        (page as AbstractPage).isDataProcessed = false
                        page.isFileExists = false
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            storage!!.restoreState(bookInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            progress.finish()
        }

        pagesBefore = pagesStream.filter { it.isFileExists }.count()
    }
}
