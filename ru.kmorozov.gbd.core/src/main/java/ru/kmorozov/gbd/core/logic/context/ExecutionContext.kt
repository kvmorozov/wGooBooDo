package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.Proxy.UrlType
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.gbd.logger.progress.IProgress
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.ToLongFunction

/**
 * Created by km on 22.11.2015.
 */
class ExecutionContext private constructor(val output: AbstractOutputReceiver, val isSingleMode: Boolean) {
    private val bookContextMap: MutableMap<String, BookContext> = ConcurrentHashMap<String, BookContext>()
    lateinit var bookExecutor: QueuedThreadPoolExecutor<BookContext>
    lateinit var pdfExecutor: QueuedThreadPoolExecutor<BookContext>

    lateinit var defaultMetadata: ILibraryMetadata

    val bookIds: Iterable<String>
        get() = bookContextMap.keys

    fun getLogger(clazz: Class<*>, bookContext: BookContext? = null): Logger {
        return Logger.getLogger(output, clazz.name, if (isSingleMode || null == bookContext) EMPTY else bookContext.bookInfo.bookData.title + ": ")
    }

    fun getLogger(location: String): Logger {
        return Logger.getLogger(output, location, EMPTY)
    }

    fun addBookContext(idsProducer: IBookListProducer, progress: IProgress, postProcessor: IPostProcessor) {
        idsProducer.bookIds.stream().parallel().forEach {
            try {
                bookContextMap.computeIfAbsent(it) { BookContext(it, progress, postProcessor) }
            } catch (ex: Exception) {
                logger.severe("Cannot add book $it because of $ex.message")
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

        val contexts = getContexts(true)

        for (bookContext in contexts) {
            val extractor = bookContext.extractor
            extractor.newProxyEvent(HttpHostExt.NO_PROXY)
            bookExecutor.execute(extractor)
        }

        defaultMetadata = LibraryFactory.getMetadata(contexts)

        AbstractProxyListProvider.INSTANCE.processProxyList(UrlType.GOOGLE_BOOKS)

        bookExecutor.terminate(10L, TimeUnit.MINUTES)
        pdfExecutor.terminate(30L, TimeUnit.MINUTES)

        val totalProcessed = getContexts(false).stream().mapToLong(ToLongFunction<BookContext> { x -> (x as BookContext).pagesProcessed }).sum()
        getLogger("Total").info("Total pages processed: $totalProcessed")

        val contextProvider = ContextProvider.contextProvider

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

        public var initialized = false

        @Synchronized
        fun initContext(output: AbstractOutputReceiver, singleMode: Boolean) {
            INSTANCE = ExecutionContext(output, singleMode)
            initialized = true
        }

        fun getLogger(clazz: Class<*>): Logger {
            return Logger.getLogger(ReceiverProvider.getReceiver(), clazz.name, "")
        }

        val proxyCount: Int
            get() = AbstractProxyListProvider.INSTANCE.proxyCount

        fun sendProxyEvent(proxy: HttpHostExt) {
            INSTANCE.newProxyEvent(proxy)
        }
    }
}