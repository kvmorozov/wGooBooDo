package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.UrlType
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.progress.IProgress
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.ToLongFunction

/**
 * Created by km on 22.11.2015.
 */
class ExecutionContext private constructor(val output: AbstractOutputReceiver, val isSingleMode: Boolean) {
    private val bookContextMap = HashMap<String, BookContext>()
    lateinit var bookExecutor: QueuedThreadPoolExecutor<BookContext>
    lateinit var pdfExecutor: QueuedThreadPoolExecutor<BookContext>

    val bookIds: Iterable<String>
        get() = bookContextMap.keys

    @JvmOverloads
    fun getLogger(clazz: Class<*>, bookContext: BookContext? = null): Logger {
        return Logger.getLogger(output, clazz.name, if (isSingleMode || null == bookContext) EMPTY else bookContext.bookInfo.bookData.title + ": ")
    }

    fun getLogger(location: String): Logger {
        return Logger.getLogger(output, location, EMPTY)
    }

    fun addBookContext(idsProducer: IBookListProducer, progress: IProgress, postProcessor: IPostProcessor) {
        for (bookId in idsProducer.bookIds) {
            try {
                (bookContextMap as MutableMap<String, BookContext>).computeIfAbsent(bookId) { BookContext(bookId, progress, postProcessor) }
            } catch (ex: Exception) {
                logger.severe("Cannot add book " + bookId + " because of " + ex.message)
            }

        }
    }

    fun getContexts(shuffle: Boolean): List<BookContext> {
        val contexts = Arrays.asList(*bookContextMap.values.toTypedArray())
        if (shuffle) contexts.shuffle()
        return contexts
    }

    @Synchronized
    fun updateProxyList() {
        AbstractProxyListProvider.INSTANCE.updateProxyList()
    }

    @Synchronized
    fun updateBlacklist() {
        AbstractProxyListProvider.updateBlacklist()
    }

    fun execute() {
        bookExecutor = QueuedThreadPoolExecutor(bookContextMap.size.toLong(), 5, { x -> x.isImgStarted }, "bookExecutor")
        pdfExecutor = QueuedThreadPoolExecutor(bookContextMap.size.toLong(), 5, { x -> x.isPdfCompleted }, "pdfExecutor")

        for (bookContext in getContexts(true)) {
            val extractor = bookContext.extractor
            extractor.newProxyEvent(HttpHostExt.NO_PROXY)
            bookExecutor.execute(extractor)
        }

        AbstractProxyListProvider.INSTANCE.processProxyList(UrlType.GOOGLE_BOOKS)

        bookExecutor.terminate(10L, TimeUnit.MINUTES)
        pdfExecutor.terminate(30L, TimeUnit.MINUTES)

        val totalProcessed = getContexts(false).stream().mapToLong(ToLongFunction<BookContext> { x -> (x as BookContext).pagesProcessed }).sum()
        getLogger("Total").info("Total pages processed: $totalProcessed")

        val contextProvider = ContextProvider.getContextProvider()

        contextProvider!!.updateIndex()
        contextProvider.updateContext()
        updateBlacklist()
        AbstractHttpProcessor.close()
    }

    private fun newProxyEvent(proxy: HttpHostExt) {
        for (bookContext in getContexts(true))
            bookContext.extractor.newProxyEvent(proxy)
    }

    fun postProcessBook(bookContext: BookContext) {
        pdfExecutor.execute(bookContext.getPostProcessor())
    }

    companion object {

        internal val logger = Logger.getLogger(ExecutionContext::class.java)

        private const val EMPTY = ""
        lateinit var INSTANCE: ExecutionContext

        @Synchronized
        fun initContext(output: AbstractOutputReceiver, singleMode: Boolean) {
            INSTANCE = ExecutionContext(output, singleMode)
        }

        val proxyCount: Int
            get() = AbstractProxyListProvider.INSTANCE.proxyCount

        fun sendProxyEvent(proxy: HttpHostExt) {
            INSTANCE.newProxyEvent(proxy)
        }
    }
}