package ru.kmorozov.gbd.core.logic.context

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.library.metadata.GoogleBooksMetadata.Companion.GOOGLE_METADATA
import ru.kmorozov.gbd.core.logic.library.metadata.UnknownMetadata
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.logger.output.ReceiverProvider
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Created by km on 22.11.2015.
 */
class ExecutionContext private constructor(val isSingleMode: Boolean) {
    var defaultMetadata: ILibraryMetadata = GOOGLE_METADATA

    val bookIds: Iterable<String>
        get() = bookContextMap.keys

    fun getLogger(clazz: Class<*>, bookContext: BookContext? = null): Logger {
        return Logger.getLogger(ReceiverProvider.getReceiver(GBDOptions.debugEnabled), clazz.name, if (isSingleMode || null == bookContext) EMPTY else bookContext.bookInfo.bookData.title + ": ")
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
            }

            bookContext.resetBookInfo()
            val extractor = bookContext.extractor
            extractor.reset()

            if (GBDOptions.serverMode)
                AbstractProxyListProvider.INSTANCE.parallelProxyStream.forEach { extractor.newProxyEvent(it) }

            bookExecutor.execute(extractor)
        }

        defaultMetadata = LibraryFactory.getMetadata(contexts)
        if (defaultMetadata == UnknownMetadata.UNKNOWN_METADATA) {
            for (bookContext in contexts)
                postProcessBook(bookContext)

            pdfExecutor.reset(0)
            bookExecutor.terminate(10L, TimeUnit.MINUTES)
            pdfExecutor.terminate(30L, TimeUnit.MINUTES)

            AbstractHttpProcessor.close()
        } else {
            bookExecutor.terminate(10L, TimeUnit.MINUTES)
            pdfExecutor.terminate(30L, TimeUnit.MINUTES)

            val totalProcessed = getContexts(false).stream().mapToLong({ x -> (x as BookContext).pagesProcessed.get() }).sum()
            getLogger("Total").info("Total pages processed: $totalProcessed")

            val contextProvider = ContextProvider.contextProvider

            contextProvider.updateContext()
            updateBlacklist()
            AbstractHttpProcessor.close()
        }
    }

    fun forceComepleteOne() {
        pdfExecutor.dec()
    }

    fun forceCompleteAll() {
        bookContextMap.values.forEach { it.forceComplete() }
    }

    private fun newProxyEvent(proxy: HttpHostExt) {
        for (bookContext in getContexts(true))
            bookContext.extractor.newProxyEvent(proxy)
    }

    fun postProcessBook(bookContext: BookContext) {
        if (!bookContext.isEmpty)
            pdfExecutor.execute(bookContext.getPostProcessor())
    }

    fun inProcess(): Boolean {
        return bookContextMap.values.filter { !it.pdfCompleted.get() }.count() > 0
    }

    companion object {
        private val bookContextMap: MutableMap<String, BookContext> = ConcurrentHashMap<String, BookContext>()
        lateinit var bookExecutor: QueuedThreadPoolExecutor<BookContext>
        lateinit var pdfExecutor: QueuedThreadPoolExecutor<BookContext>
        lateinit var proxyExecutor: QueuedThreadPoolExecutor<InetSocketAddress>

        internal val logger = Logger.getLogger(GBDOptions.debugEnabled, ExecutionContext::class.java)

        private const val EMPTY = ""
        lateinit var INSTANCE: ExecutionContext

        var initialized = false

        @Synchronized
        fun initContext(singleMode: Boolean) {
            INSTANCE = ExecutionContext(singleMode)

            bookExecutor = QueuedThreadPoolExecutor(bookContextMap.size, 5, { it.isImgStarted || it.isEmpty }, "bookExecutor")
            pdfExecutor = QueuedThreadPoolExecutor(bookContextMap.size, 5, { it.isPdfCompleted || it.isEmpty }, "pdfExecutor")

            initialized = true
        }

        fun getLogger(clazz: Class<*>): Logger {
            return Logger.getLogger(ReceiverProvider.getReceiver(GBDOptions.debugEnabled), clazz.name, "")
        }

        fun getLogger(location: String): Logger {
            return Logger.getLogger(ReceiverProvider.getReceiver(), location, EMPTY)
        }

        val proxyCount: Int
            get() = AbstractProxyListProvider.INSTANCE.proxyCount

        fun sendProxyEvent(proxy: HttpHostExt) {
            INSTANCE.newProxyEvent(proxy)
        }
    }
}