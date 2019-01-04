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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractImageExtractor <T: AbstractPage> : AbstractEventSource, IUniqueRunnable<BookContext>, IImageExtractor {

    override var uniqueObject: BookContext

    protected constructor(uniqueObject: BookContext, extractorClass: Class<out AbstractImageExtractor<T>>) : super() {
        this.uniqueObject = uniqueObject
        this.initComplete = AtomicBoolean(false)
        this.waitingProxy = CopyOnWriteArrayList()
        processStatus = uniqueObject.progress
        logger = ExecutionContext.INSTANCE.getLogger(extractorClass, uniqueObject)
        this.output = ExecutionContext.INSTANCE.output
    }

    protected val output: AbstractOutputReceiver
    protected val initComplete: AtomicBoolean
    protected val logger: Logger
    protected var waitingProxy: MutableList<HttpHostExt>

    override fun toString(): String {
        return "Extractor:$uniqueObject"
    }

    fun process() {
        if (!uniqueObject.started.compareAndSet(false, true)) return

        if (!preCheck()) return

        prepareStorage()
        uniqueObject.restoreState()

        initComplete.set(true)
    }

    protected open fun preCheck(): Boolean {
        return true
    }

    override fun run() {
        process()

        while (!initComplete.get()) {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        waitingProxy.forEach(Consumer<HttpHostExt> { this.newProxyEvent(it) })
    }

    protected open fun prepareStorage() {
        if (!GBDOptions.storage.isValidOrCreate) return

        logger.info(if (ExecutionContext.INSTANCE.isSingleMode) String.format("Working with %s", uniqueObject.bookInfo.bookData.title) else "Starting...")

        try {
            uniqueObject.storage = GBDOptions.storage.getChildStorage(uniqueObject.bookInfo.bookData)
            uniqueObject.progress.resetMaxValue(uniqueObject.storage.size())
        } catch (e: IOException) {
            logger.error(e)
        }

        if (!uniqueObject.storage.isValidOrCreate)
            logger.severe(String.format("Invalid book title: %s", uniqueObject.bookInfo.bookData.title))
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        if (!proxy.isLocal) return

        Thread(EventProcessor(proxy)).start()
    }

    protected inner class EventProcessor internal constructor(private val proxy: HttpHostExt) : Runnable {

        override fun run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy)
                return
            }

            for (page in uniqueObject.bookInfo.pages.pages)
                uniqueObject.imgExecutor.execute(SimplePageImgProcessor(uniqueObject, page as T, HttpHostExt.NO_PROXY))

            uniqueObject.imgExecutor.terminate(20L, TimeUnit.MINUTES)

            ExecutionContext.INSTANCE.updateProxyList()

            logger.info(uniqueObject.bookInfo.pages.missingPagesList)

            val pagesAfter = uniqueObject.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

            logger.info(String.format("Processed %s pages", pagesAfter - uniqueObject.pagesBefore))

            synchronized(uniqueObject) {
                ExecutionContext.INSTANCE.postProcessBook(uniqueObject)
            }
        }
    }
}
