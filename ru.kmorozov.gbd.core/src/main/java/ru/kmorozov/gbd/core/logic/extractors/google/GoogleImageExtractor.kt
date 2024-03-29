package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.common.base.Strings
import ru.kmorozov.db.core.logic.model.book.BookInfo.Companion.EMPTY_BOOK
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * Created by km on 21.11.2015.
 */
class GoogleImageExtractor(bookContext: BookContext) :
    AbstractImageExtractor<GooglePageInfo>(bookContext, GoogleImageExtractor::class.java) {

    private val proxyReceived = AtomicInteger(0)
    private val processingStarted = AtomicBoolean(false)
    private val initComplete = AtomicBoolean(false)
    private var waitingProxy: MutableList<HttpHostExt> = CopyOnWriteArrayList()

    override fun preCheck(): Boolean {
        if (uniqueObject.bookInfo.empty || uniqueObject.bookInfo.pages.pages.isEmpty())
            uniqueObject.bookInfo = GoogleBookInfoExtractor(uniqueObject.bookInfo.bookId).findBookInfo()

        return if (!Strings.isNullOrEmpty((uniqueObject.bookInfo.bookData as GoogleBookData).flags!!.downloadPdfUrl)) {
            logger.severe("There is direct url to download book. DIY!")
            uniqueObject.forceComplete()
            false
        } else
            true
    }

    override fun process() {
        if (uniqueObject.bookInfo == EMPTY_BOOK)
            return

        super.process()

        initComplete.set(true)
    }

    override fun reset() {
        processingStarted.set(false)
        proxyReceived.set(0)
    }

    override fun run() {
        super.run()

        waitingProxy.forEach(Consumer { this.newProxyEvent(it) })
    }

    override fun processProxyEvent(proxy: HttpHostExt) {
        while (!initComplete.get()) {
            waitingProxy.add(proxy)
            return
        }

        if (GBDOptions.debugEnabled)
            logger.info("Received proxy event for $proxy")

        if (uniqueObject.pdfCompleted.get())
            return

        if (proxy.isAvailable) uniqueObject.sigExecutor.execute(GooglePageSigProcessor(uniqueObject, proxy))

        val proxyNeeded = ExecutionContext.proxyCount - proxyReceived.incrementAndGet()

        if (0 >= proxyNeeded) {
            if (!processingStarted.compareAndSet(false, true)) return

            uniqueObject.sigExecutor.terminate(10L, TimeUnit.MINUTES)

            uniqueObject.pagesStream
                .filter { page -> !page.isDataProcessed }
                .sorted { p1, p2 -> p2.order - p1.order }
                .forEach { page ->
                    uniqueObject.imgExecutor
                        .execute(GooglePageImgProcessor(uniqueObject, page as GooglePageInfo, HttpHostExt.NO_PROXY))
                }

            ExecutionContext.proxyExecutor.terminate(3L, TimeUnit.MINUTES)
            uniqueObject.imgExecutor.terminate(10L, TimeUnit.MINUTES)

            logger.info(uniqueObject.bookInfo.pages.missingPagesList)

            val pagesAfter = uniqueObject.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

            uniqueObject.pagesProcessed.set(pagesAfter - uniqueObject.pagesBefore)
            logger.info("Processed ${uniqueObject.pagesProcessed} pages")

            ExecutionContext.INSTANCE.postProcessBook(uniqueObject)
        }
    }
}
