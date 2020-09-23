package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.logic.context.BookTask
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.context.IBookListProducer
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.proxy.providers.AbstractProxyListProvider
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import ru.kmorozov.library.data.loader.processors.gbd.workers.PageWorker
import ru.kmorozov.library.data.loader.processors.gbd.workers.SigWorker
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class ServerContext {

    private val sigTaskQueue: Queue<BookTask> = ConcurrentLinkedQueue<BookTask>()
    private val pageTaskQueue: Queue<BookTask> = ConcurrentLinkedQueue<BookTask>()
    private val sigWorker = SigWorker(sigTaskQueue, pageTaskQueue)
    private val pageWorker = PageWorker(pageTaskQueue)

    private lateinit var sigExecutor: QueuedThreadPoolExecutor<SigWorker>
    private lateinit var pagesExecutor: QueuedThreadPoolExecutor<PageWorker>

    fun updateBookList(idsProducer: IBookListProducer) {
        ExecutionContext.INSTANCE.addBookContext(idsProducer, ServerPostProcessor())
    }

    fun execute() {
        if (ExecutionContext.INSTANCE.size() == 0)
            return

        if (inProcess()) {
            tryTerminate()
            return
        }

        val contexts = ExecutionContext.INSTANCE.getContexts(true)

        sigExecutor = QueuedThreadPoolExecutor(-1, 5, { it.workComplete() }, "sigExecutor")
        pagesExecutor = QueuedThreadPoolExecutor(-1, 5, { it.workComplete() }, "pagesExecutor")

        for (bookContext in contexts) {
            if (bookContext.isPdfCompleted) {
                bookContext.started.set(false)
                bookContext.pdfCompleted.set(false)
            }

            ExecutionContext.INSTANCE.defaultMetadata = LibraryFactory.getMetadata(contexts)

            bookContext.resetBookInfo()
            bookContext.pagesStream
                    .filter { !it.isDataProcessed }
                    .forEach { page ->
                        run {
                            sigTaskQueue.addAll(
                                    AbstractProxyListProvider.INSTANCE.proxyList
                                            .map { proxy -> BookTask(bookContext, page, proxy) }.toList())
                        }
                    }
        }

        if (sigExecutor.activeCount < sigExecutor.corePoolSize)
            repeat(sigExecutor.corePoolSize) {
                sigExecutor.execute { sigWorker.doWork() }
            }

        if (pagesExecutor.activeCount < pagesExecutor.corePoolSize)
            repeat(pagesExecutor.corePoolSize) {
                pagesExecutor.execute { pageWorker.doWork() }
            }
    }

    private fun tryTerminate() {
        if (sigTaskQueue.isEmpty() && pageTaskQueue.isEmpty())
            for (bookContext in ExecutionContext.INSTANCE.getContexts(true))
                ExecutionContext.INSTANCE.postProcessBook(bookContext)
    }

    private fun inProcess(): Boolean {
        return ExecutionContext.INSTANCE.inProcess()
    }
}