package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.common.base.Strings
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor
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
        if (page.bookInfo.empty || page.bookInfo.pages.pages.size == 0)
            page.bookInfo = GoogleBookInfoExtractor(page.bookInfo.bookId).findBookInfo()

        if (!Strings.isNullOrEmpty((page.bookInfo.bookData as GoogleBookData).flags!!.downloadPdfUrl)) {
            logger.severe("There is direct url to download book. DIY!")
            return false
        } else
            return true
    }

    override fun prepareStorage() {
        super.prepareStorage()
        page.bookInfo.pages.build()
    }

    override fun newProxyEvent(proxy: HttpHostExt) {
        Thread(EventProcessor(proxy)).start()
    }

    override fun process() {
        super.process()

        initComplete.set(true)
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

            if (proxy.isAvailable) page.sigExecutor.execute(GooglePageSigProcessor(page, proxy))

            val proxyNeeded = ExecutionContext.proxyCount - proxyReceived.incrementAndGet()

            if (0 >= proxyNeeded) {
                if (!processingStarted.compareAndSet(false, true)) return

                page.sigExecutor.terminate(10L, TimeUnit.MINUTES)

                page.pagesStream
                        .filter { page -> !page.isDataProcessed && null != (page as GooglePageInfo).sig }
                        .sorted(Comparator {p1, p2 -> p2.order - p1.order})
                        .forEach { page -> this@GoogleImageExtractor.page.imgExecutor.execute(GooglePageImgProcessor(this@GoogleImageExtractor.page, page as GooglePageInfo, HttpHostExt.NO_PROXY)) }

                page.imgExecutor.terminate(10L, TimeUnit.MINUTES)

                logger.info(page.bookInfo.pages.missingPagesList)

                val pagesAfter = page.pagesStream.filter { pageInfo -> pageInfo.isDataProcessed }.count()

                page.pagesProcessed = pagesAfter - page.pagesBefore
                logger.info("Processed ${page.pagesProcessed} pages")

                ExecutionContext.INSTANCE.postProcessBook(page)
            }
        }
    }
}
