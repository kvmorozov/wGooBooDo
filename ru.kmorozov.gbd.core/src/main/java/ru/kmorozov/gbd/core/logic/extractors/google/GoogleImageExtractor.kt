package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.common.base.Strings
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.progress.IProgress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * Created by km on 21.11.2015.
 */
class GoogleImageExtractor(bookContext: BookContext) : AbstractImageExtractor<GooglePageInfo>(bookContext, GoogleImageExtractor::class.java) {

    override var processStatus: IProgress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    private val proxyReceived = AtomicInteger(0)
    private val processingStarted = AtomicBoolean(false)
    private val initComplete = AtomicBoolean(false)
    private var waitingProxy: MutableList<HttpHostExt> = CopyOnWriteArrayList()

    override fun preCheck(): Boolean {
        if (uniqueObject.bookInfo.empty || uniqueObject.bookInfo.pages.pages.size == 0)
            uniqueObject.bookInfo = GoogleBookInfoExtractor(uniqueObject.bookInfo.bookId).findBookInfo()

        if (!Strings.isNullOrEmpty((uniqueObject.bookInfo.bookData as GoogleBookData).flags!!.downloadPdfUrl)) {
            logger.severe("There is direct url to download book. DIY!")
            return false
        } else
            return true
    }

    override fun prepareStorage() {
        super.prepareStorage()
        uniqueObject.bookInfo.pages.build()
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        Thread(EventProcessor(proxy)).start()
    }

    override fun process() {
        super.process()

        uniqueObject.restoreState()
        initComplete.set(true)
    }

    override fun reset() {
        processingStarted.set(false)
        proxyReceived.set(0)
    }

    override fun run() {
        super.run()

        waitingProxy.forEach(Consumer<HttpHostExt> { this.newProxyEvent(it) })
    }

    private inner class EventProcessor internal constructor(private val proxy: HttpHostExt) : Runnable {

        override fun run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy)
                return
            }

            if (proxy.isAvailable) uniqueObject.sigExecutor.execute(GooglePageSigProcessor(uniqueObject, proxy))

            val proxyNeeded = ExecutionContext.proxyCount - proxyReceived.incrementAndGet()

            if (0 >= proxyNeeded) {
                if (!processingStarted.compareAndSet(false, true)) return

                uniqueObject.sigExecutor.terminate(10L, TimeUnit.MINUTES)

                uniqueObject.pagesStream
                        .filter { page -> !page.isDataProcessed }
                        .sorted(Comparator { p1, p2 -> p2.order - p1.order })
                        .forEach { page ->
                            this@GoogleImageExtractor.uniqueObject.imgExecutor
                                    .execute(GooglePageImgProcessor(this@GoogleImageExtractor.uniqueObject, page as GooglePageInfo, HttpHostExt.NO_PROXY))
                        }

                uniqueObject.imgExecutor.terminate(10L, TimeUnit.MINUTES)

                logger.info(uniqueObject.bookInfo.pages.missingPagesList)

                val pagesAfter = uniqueObject.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

                uniqueObject.pagesProcessed = pagesAfter - uniqueObject.pagesBefore
                logger.info("Processed ${uniqueObject.pagesProcessed} pages")

                ExecutionContext.INSTANCE.postProcessBook(uniqueObject)
            }
        }
    }
}
