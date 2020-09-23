package ru.kmorozov.library.data.loader.processors.gbd.workers

import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.logic.context.BookTask
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.google.GooglePageImgProcessor
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PageWorker(private val pageTaskQueue: Queue<BookTask>) {

    fun doWork() {
        while (true) {
            val task = pageTaskQueue.poll()
            if (task == null) {
                Thread.sleep(1000L)
                continue
            } else {
                val pageProcessor = GooglePageImgProcessor(task.book, task.page as GooglePageInfo, task.proxy)
                pageProcessor.run()

                if (counter.incrementAndGet() % 10 == 0)
                    logger.info("PageWorker completed ${counter.get()} tasks")

                if (task.page.isDataProcessed)
                    task.book.pagesProcessed.incrementAndGet()
            }
        }
    }

    fun workComplete(): Boolean {
        return pageTaskQueue.size == 0
    }

    companion object {
        private val counter = AtomicInteger(0)
        private val logger = ExecutionContext.INSTANCE.getLogger("PageWorker")
    }
}