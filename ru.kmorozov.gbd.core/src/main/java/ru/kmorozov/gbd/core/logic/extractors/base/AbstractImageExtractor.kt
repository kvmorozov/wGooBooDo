package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.events.AbstractEventSource
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractImageExtractor<T : AbstractPage> : AbstractEventSource, IUniqueRunnable<BookContext>, IImageExtractor {

    override var page: BookContext

    protected constructor(uniqueObject: BookContext, extractorClass: Class<out AbstractImageExtractor<T>>) : super() {
        this.page = uniqueObject
        processStatus = uniqueObject.progress
        logger = ExecutionContext.INSTANCE.getLogger(extractorClass, uniqueObject)
        this.output = ExecutionContext.INSTANCE.output
    }

    protected val output: AbstractOutputReceiver
    protected val logger: Logger

    override fun toString(): String {
        return "Extractor:$page"
    }

    open fun process() {
        if (!page.started.compareAndSet(false, true)) return

        if (!preCheck()) return

        prepareStorage()
    }

    protected open fun preCheck(): Boolean {
        return true
    }

    override fun run() {
        process()
    }

    protected open fun prepareStorage() {
        if (!GBDOptions.storage.isValidOrCreate) return

        logger.info(if (ExecutionContext.INSTANCE.isSingleMode) "Working with ${page.bookInfo.bookData.title}" else "Starting...")

        try {
            page.storage = GBDOptions.storage.getChildStorage(page.bookInfo.bookData)
            page.progress.resetMaxValue(page.storage.size())
        } catch (e: IOException) {
            logger.error(e)
        }

        if (!page.storage.isValidOrCreate)
            logger.severe("Invalid book title: ${page.bookInfo.bookData.title}")
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        if (!proxy.isLocal) return

        Thread(EventProcessor(proxy)).start()
    }

    protected inner class EventProcessor internal constructor(private val proxy: HttpHostExt) : Runnable {

        override fun run() {
            for (page in page.bookInfo.pages.pages)
                this@AbstractImageExtractor.page.imgExecutor.execute(SimplePageImgProcessor(this@AbstractImageExtractor.page, page as T, HttpHostExt.NO_PROXY))

            page.imgExecutor.terminate(20L, TimeUnit.MINUTES)

            ExecutionContext.INSTANCE.updateProxyList()

            logger.info(page.bookInfo.pages.missingPagesList)

            val pagesAfter = page.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

            logger.info("Processed ${pagesAfter - page.pagesBefore} pages")

            synchronized(page) {
                ExecutionContext.INSTANCE.postProcessBook(page)
            }
        }
    }
}
