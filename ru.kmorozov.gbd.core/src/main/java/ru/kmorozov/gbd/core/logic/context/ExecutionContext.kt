package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.UrlType
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.consumers.AbstractOutputReceiver
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.function.ToLongFunction

/**
 * Created by km on 22.11.2015.
 */
class ExecutionContext private constructor(val output: AbstractOutputReceiver, val isSingleMode: Boolean) {
    lateinit var defaultMetadata: ILibraryMetadata
    private val mainTaskQueue: Queue<BookTask> = ConcurrentLinkedQueue<BookTask>()
    private val auxTaskQueue: Queue<BookTask> = ConcurrentLinkedQueue<BookTask>()

    val bookIds: Iterable<String>
        get() = bookContextMap.keys

    fun getLogger(clazz: Class<*>, bookContext: BookContext? = null): Logger {
        return Logger.getLogger(output, clazz.name, if (isSingleMode || null == bookContext) EMPTY else bookContext.bookInfo.bookData.title + ": ")
    }

    fun getLogger(location: String): Logger {
        return Logger.getLogger(output, location, EMPTY)
    }

    fun addBookContext(idsProducer: IBookListProducer, postProcessor: IPostProcessor) {
        idsProducer.bookIds.stream().parallel().forEach {
            try {
                bookContextMap.computeIfAbsent(it) { BookContext(it, postProcessor) }
            } catch (ex: Exception) {
                logger.severe("Cannot add book $it because of $ex.message")
            }
        }
    }

    fun getContexts(shuffle: Boolean): List<BookContext> {
        val contexts = mutableListOf(*bookContextMap.values.toTypedArray())
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

    fun size(): Int {
        return bookContextMap.size
    }

    fun execute() {
        val contexts = getContexts(true)

        bookExecutor.reset(contexts.size)
        pdfExecutor.reset(contexts.size)

        for (bookContext in contexts) {
            if (bookContext.isPdfCompleted) {
                bookContext.started.set(false)
                bookContext.pdfCompleted.set(false)

                val extractor = bookContext.extractor
                extractor.reset()

                if (GBDOptions.serverMode)
                    AbstractProxyListProvider.INSTANCE.parallelProxyStream.forEach { extractor.newProxyEvent(it) }

                bookExecutor.execute(extractor)
            }
        }

        defaultMetadata = LibraryFactory.getMetadata(contexts)

        if (!GBDOptions.serverMode) {
            AbstractProxyListProvider.INSTANCE.findCandidates()
            AbstractProxyListProvider.INSTANCE.processProxyList(UrlType.GOOGLE_BOOKS)
        }

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

    public fun inProcess(): Boolean {
        return bookContextMap.values.filter { !it.pdfCompleted.get() }.count() > 0
    }

    companion object {
        private val bookContextMap: MutableMap<String, BookContext> = ConcurrentHashMap<String, BookContext>()
        lateinit var bookExecutor: QueuedThreadPoolExecutor<BookContext>
        lateinit var pdfExecutor: QueuedThreadPoolExecutor<BookContext>

        internal val logger = Logger.getLogger(ExecutionContext::class.java)

        private const val EMPTY = ""
        lateinit var INSTANCE: ExecutionContext

        public var initialized = false

        @Synchronized
        fun initContext(output: AbstractOutputReceiver, singleMode: Boolean) {
            INSTANCE = ExecutionContext(output, singleMode)

            bookExecutor = QueuedThreadPoolExecutor(bookContextMap.size, 5, { x -> x.isImgStarted }, "bookExecutor")
            pdfExecutor = QueuedThreadPoolExecutor(bookContextMap.size, 5, { x -> x.isPdfCompleted }, "pdfExecutor")

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