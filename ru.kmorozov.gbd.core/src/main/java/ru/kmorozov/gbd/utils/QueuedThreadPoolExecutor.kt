package ru.kmorozov.gbd.utils

import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueReusable
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by km on 12.11.2016.
 */
class QueuedThreadPoolExecutor<T : Any> : ThreadPoolExecutor {
    private var needProcessCount: AtomicInteger
    private val completeChecker: (T) -> Boolean
    private val description: String
    private val reusablePool: Queue<IUniqueReusable<T>> = ConcurrentLinkedQueue()

    constructor(needProcessCount: Int, threadPoolSize: Int, completeChecker: (T) -> Boolean, description: String) :
            super(
                threadPoolSize,
                threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                ArrayBlockingQueue(RETENTION_QUEUE_SIZE),
                NamedThreadFactory(description)
            ) {
        this.needProcessCount = AtomicInteger(needProcessCount)
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

    fun reset(needProcessCount: Int) {
        this.needProcessCount = AtomicInteger(needProcessCount)
    }

    fun dec() {
        needProcessCount.decrementAndGet()
    }

    fun terminate(timeout: Long, unit: TimeUnit) {
        val liveTime = Math.min(unit.toMillis(timeout), MAX_LIVE_TIME)
        val counter = AtomicInteger(0)
        var submitted = 0
        while (true)
            try {
                val completed = uniqueMap.keys.filter(completeChecker).count()
                if (completedTaskCount == taskCount && completedTaskCount >= needProcessCount.get() || System.currentTimeMillis() - timeStart > liveTime)
                    break
                if (0 == counter.incrementAndGet() % 100) {
                    if (0L < needProcessCount.get())
                        logger.finest(
                            String.format(
                                "Waiting for %s %d sec (%d of %d completed, %d tasks finished of %d submitted, %d in queue)",
                                description,
                                counter.get(),
                                completed,
                                needProcessCount,
                                completedTaskCount,
                                taskCount,
                                queue.size
                            )
                        )

                    if (submitted == taskCount.toInt() && 0L < taskCount && submitted < needProcessCount.get()) {
                        logger.severe(
                            String.format(
                                "Nothing was submitted to %s, set needProcessCount to %d",
                                description,
                                submitted
                            )
                        )
                        needProcessCount = AtomicInteger(submitted)
                    } else
                        submitted = taskCount.toInt()
                }
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                logger.severe("Wait interrupted for $description")
            }

        if (0L < needProcessCount.get())
            logger.finest(
                "Terminating working threads for $description after ${counter.get()} sec (${
                    uniqueMap.keys.filter(
                        completeChecker
                    ).count()
                } of $needProcessCount completed, $completedTaskCount tasks finished of $taskCount submitted)"
            )
        shutdownNow()

        uniqueMap.clear()
    }

    fun returnReusable(runner: IUniqueReusable<T>) {
        reusablePool.offer(runner)
    }

    override fun execute(command: Runnable) {
        if (command is IUniqueRunnable<*>) {
            val uniqueObj = (command as IUniqueRunnable<T>).uniqueObject

            var commandToRun = command

            if (command is IUniqueReusable<*>) {
                (command as IUniqueReusable<T>).reuseCallback = this::returnReusable
                if (reusablePool.size > 0) {
                    commandToRun = reusablePool.poll()
                    if (!commandToRun.initReusable(command))
                        commandToRun = command
                }
            }

            synchronized(uniqueObj) {
                if (null == uniqueMap.putIfAbsent(uniqueObj, commandToRun as IUniqueRunnable<T>)) super.execute(
                    commandToRun
                )
            }
        } else
            super.execute(command)
    }

    companion object {

        const val THREAD_POOL_SIZE = 10
        private val logger = ExecutionContext.getLogger("Executor")
        private const val RETENTION_QUEUE_SIZE = 200
        private val MAX_LIVE_TIME = TimeUnit.HOURS.toMillis(1L)
    }
}
