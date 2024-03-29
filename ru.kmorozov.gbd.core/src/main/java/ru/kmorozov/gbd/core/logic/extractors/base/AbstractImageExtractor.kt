package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.events.AbstractEventSource
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import java.util.concurrent.TimeUnit

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
abstract class AbstractImageExtractor<T : AbstractPage> protected constructor(
    override var uniqueObject: BookContext,
    extractorClass: Class<out AbstractImageExtractor<T>>
) : AbstractEventSource(), IUniqueRunnable<BookContext>, IImageExtractor {

    protected val output: AbstractOutputReceiver
    protected val logger: Logger

    override fun toString(): String {
        return "Extractor:$uniqueObject"
    }

    open fun process() {
        if (uniqueObject.started.get()) return

        if (!preCheck()) return

        synchronized(uniqueObject) {
            if (uniqueObject.started.get()) return
            uniqueObject.restoreState()
            uniqueObject.started.set(true)

            AbstractProxyListProvider.INSTANCE.parallelProxyStream.forEach { ExecutionContext.sendProxyEvent(it) }
        }
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

        while (!uniqueObject.started.get()) {
            Thread.sleep(1000L)
        }

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

    init {
        logger = ExecutionContext.INSTANCE.getLogger(extractorClass, uniqueObject)
        this.output = ReceiverProvider.getReceiver(GBDOptions.debugEnabled)
    }
}
