package ru.kmorozov.gbd.utils

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * Created by km on 12.11.2016.
 */
class QueuedThreadPoolExecutor<T> : ThreadPoolExecutor {
    private var needProcessCount: Long
    private val completeChecker: (T) -> Boolean
    private val description: String

    constructor(needProcessCount: Long, threadPoolSize: Int, completeChecker: (T) -> Boolean, description: String) : super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(RETENTION_QUEUE_SIZE)) {
        this.needProcessCount = needProcessCount
        this.completeChecker = completeChecker
        this.description = description
        this.uniqueMap = ConcurrentHashMap<T, IUniqueRunnable<T>>()
        this.timeStart = System.currentTimeMillis()
        setRejectedExecutionHandler { r, _ ->
            try {
                if (r is IUniqueRunnable<*>) {
                    synchronized((r as IUniqueRunnable<T>).uniqueObject as Any) {
                        if (!completeChecker.invoke(r.uniqueObject)) queue.put(r)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private val timeStart: Long
    private val uniqueMap: ConcurrentHashMap<T, IUniqueRunnable<T>>

    fun terminate(timeout: Long, unit: TimeUnit) {
        val liveTime = Math.min(unit.toMillis(timeout), MAX_LIVE_TIME)
        var counter = 0
        var submitted = 0L
        while (true)
            try {
                val completed = uniqueMap.keys.stream().filter(completeChecker).count()
                if (completedTaskCount == taskCount && completedTaskCount >= needProcessCount || System.currentTimeMillis() - timeStart > liveTime)
                    break
                if (0 == ++counter % 100) {
                    if (0L < needProcessCount)
                        logger.finest(String.format("Waiting for %s %d sec (%d of %d completed, %d tasks finished of %d submitted, %d in queue)", description, counter, completed,
                                needProcessCount, completedTaskCount, taskCount, queue.size))

                    if (submitted == taskCount && 0L < taskCount && submitted < needProcessCount) {
                        logger.severe(String.format("Nothing was submitted to %s, set needProcessCount to %d", description, submitted))
                        needProcessCount = submitted
                    } else
                        submitted = taskCount
                }
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                logger.severe("Wait interrupted for $description")
            }

        if (0L < needProcessCount)
            logger.finest(String.format("Terminating working threads for %s after %s sec (%s of %s completed, %s tasks finished of %s submitted)", description, counter, uniqueMap.keys
                    .stream()
                    .filter(completeChecker).count(), needProcessCount, completedTaskCount, taskCount))
        shutdownNow()

        uniqueMap.clear()
    }

    override fun execute(command: Runnable) {
        if (command is IUniqueRunnable<*>) {
            val uniqueObj = (command as IUniqueRunnable<T>).uniqueObject
            synchronized(uniqueObj as Any) {
                if (null == uniqueMap.put(uniqueObj, command)) super.execute(command)
            }
        } else
            super.execute(command)
    }

    companion object {

        const val THREAD_POOL_SIZE = 10
        private val logger = ExecutionContext.INSTANCE.getLogger("Executor")
        private const val RETENTION_QUEUE_SIZE = 200
        private val MAX_LIVE_TIME = TimeUnit.HOURS.toMillis(1L)
    }
}