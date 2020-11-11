package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.events.AbstractEventSource
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractImageExtractor<T : AbstractPage> : AbstractEventSource, IUniqueRunnable<BookContext>, IImageExtractor {

    override var uniqueObject: BookContext

    protected constructor(uniqueObject: BookContext, extractorClass: Class<out AbstractImageExtractor<T>>) : super() {
        this.uniqueObject = uniqueObject
        logger = ExecutionContext.INSTANCE.getLogger(extractorClass, uniqueObject)
        this.output = ReceiverProvider.getReceiver(GBDOptions.debugEnabled)
    }

    protected val output: AbstractOutputReceiver
    protected val logger: Logger

    override fun toString(): String {
        return "Extractor:$uniqueObject"
    }

    open fun process() {
        if (!uniqueObject.started.compareAndSet(false, true)) return

        if (!preCheck()) return

        uniqueObject.restoreState()
    }

    protected open fun preCheck(): Boolean {
        return true
    }

    override fun run() {
        process()
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        if (proxy.isAvailable) {
            val trEvent = Thread { processProxyEvent(proxy) }
            trEvent.start()
        }
    }

    override fun reset() {

    }

    protected open fun processProxyEvent(proxy: HttpHostExt) {
        if (!proxy.isLocal) return

        for (page in uniqueObject.bookInfo.pages.pages)
            uniqueObject.imgExecutor.execute(SimplePageImgProcessor(uniqueObject, page as T, proxy))

        uniqueObject.imgExecutor.terminate(20L, TimeUnit.MINUTES)

        ExecutionContext.INSTANCE.updateProxyList()

        logger.info(uniqueObject.bookInfo.pages.missingPagesList)

        val pagesAfter = uniqueObject.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

        logger.info("Processed ${pagesAfter - uniqueObject.pagesBefore} pages")

        synchronized(uniqueObject) {
            ExecutionContext.INSTANCE.postProcessBook(uniqueObject)
        }
    }
}
