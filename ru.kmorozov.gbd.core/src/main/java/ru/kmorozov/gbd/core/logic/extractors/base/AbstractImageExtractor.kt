package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.events.AbstractEventSource

import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractImageExtractor : AbstractEventSource, IUniqueRunnable<BookContext>, IImageExtractor {

    override var uniqueObject: BookContext

    protected constructor(uniqueObject: BookContext, extractorClass: Class<out AbstractImageExtractor>) : super() {
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

    protected abstract fun preCheck(): Boolean

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
}
