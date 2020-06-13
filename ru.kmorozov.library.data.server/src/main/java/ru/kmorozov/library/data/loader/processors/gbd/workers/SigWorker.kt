package ru.kmorozov.library.data.loader.processors.gbd.workers

import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.gbd.core.logic.context.BookTask
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.google.SigProcessorInternal
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class SigWorker(private val sigTaskQueue: Queue<BookTask>, private val pageTaskQueue: Queue<BookTask>) {

    fun doWork() {
        while (true) {
            val task = sigTaskQueue.poll()
            if (task == null)
                return
            else {
                val sigProcessor = SigProcessorInternal(task.book, task.proxy, task.page as GooglePageInfo)
                sigProcessor.run()

                if (counter.incrementAndGet() % 500 == 0)
                    logger.info("SigWorker completed ${counter.get()} tasks")

                if (sigProcessor.sigFound)
                    pageTaskQueue.offer(task)
            }
        }
    }

    fun workComplete(): Boolean {
        return sigTaskQueue.size == 0
    }

    companion object {
        private val counter = AtomicInteger(0)
        private val logger = ExecutionContext.INSTANCE.getLogger("SigWorker")
    }
}