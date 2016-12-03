package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Created by km on 12.11.2016.
 */
public class QueuedThreadPoolExecutor<T> extends ThreadPoolExecutor {

    private final static Logger logger = ExecutionContext.INSTANCE.getLogger("Executor");

    private final long needProcessCount;
    private final long timeStart;

    public static final int THREAD_POOL_SIZE = 10;
    private static final int RETENTION_QUEUE_SIZE = 200;
    private static final long MAX_LIVE_TIME = 1 * 60 * 60 * 1000;

    private final Map<T, IUniqueRunnable<T>> uniqueMap = new ConcurrentHashMap<>();
    private final Predicate<T> completeChecker;
    private String description;

    public QueuedThreadPoolExecutor(long needProcessCount, int threadPoolSize, Predicate<T> completeChecker, String description) {
        super(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(RETENTION_QUEUE_SIZE));
        this.needProcessCount = needProcessCount;
        this.timeStart = System.currentTimeMillis();
        this.completeChecker = completeChecker;
        this.description = description;

        setRejectedExecutionHandler((r, executor) -> {
            try {
                if (r instanceof IUniqueRunnable) {
                    synchronized (((IUniqueRunnable) r).getUniqueObject()) {
                        if (!completeChecker.test((T) ((IUniqueRunnable) r).getUniqueObject())) getQueue().put(r);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(long timeout, TimeUnit unit) {
        Long liveTime = Math.min(unit.toMillis(timeout), MAX_LIVE_TIME);
        int counter = 0;
        while (true) try {
            long completed = uniqueMap.keySet().stream().filter(completeChecker).count();
            if ((getCompletedTaskCount() == getTaskCount() && getCompletedTaskCount() == needProcessCount) || System.currentTimeMillis() - timeStart > liveTime)
                break;
            if (++counter % 100 == 0)
                if (needProcessCount > 0)
                    logger.finest(String.format("Waiting for %s %s sec (%s of %s completed, %s tasks finished of %s submitted, %s in queue)",
                            description, counter, completed, needProcessCount, getCompletedTaskCount(), getTaskCount(), getQueue().size()));
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.severe("Wait interrupted for " + description);
        }

        if (needProcessCount > 0)
            logger.finest(String.format("Terminating working threads for %s after %s sec (%s of %s completed, %s tasks finished of %s submitted)",
                    description, counter, uniqueMap.keySet().stream().filter(completeChecker).count(), needProcessCount, getCompletedTaskCount(), getTaskCount()));
        shutdownNow();

        uniqueMap.clear();
    }

    @Override
    public void execute(final Runnable command) {
        if (command instanceof IUniqueRunnable) synchronized (this) {
            T uniqueObj = (T) ((IUniqueRunnable) command).getUniqueObject();
            if (uniqueMap.put(uniqueObj, (IUniqueRunnable<T>) command) == null) super.execute(command);
        }
        else super.execute(command);
    }
}
