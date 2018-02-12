package ru.kmorozov.gbd.utils;

import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable;
import ru.kmorozov.gbd.logger.Logger;

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

    public static final int THREAD_POOL_SIZE = 10;
    private static final Logger logger = ExecutionContext.INSTANCE.getLogger("Executor");
    private static final int RETENTION_QUEUE_SIZE = 200;
    private static final long MAX_LIVE_TIME = TimeUnit.HOURS.toMillis(1);
    private final long timeStart;
    private final Map<T, IUniqueRunnable<T>> uniqueMap = new ConcurrentHashMap<>();
    private final Predicate<T> completeChecker;
    private long needProcessCount;
    private final String description;

    public QueuedThreadPoolExecutor(final long needProcessCount, final int threadPoolSize, final Predicate<T> completeChecker, final String description) {
        super(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(RETENTION_QUEUE_SIZE));
        this.needProcessCount = needProcessCount;
        this.timeStart = System.currentTimeMillis();
        this.completeChecker = completeChecker;
        this.description = description;

        setRejectedExecutionHandler((r, executor) -> {
            try {
                if (r instanceof IUniqueRunnable) {
                    synchronized (((IUniqueRunnable<T>) r).getUniqueObject()) {
                        if (!completeChecker.test((T) ((IUniqueRunnable<T>) r).getUniqueObject())) getQueue().put(r);
                    }
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(final long timeout, final TimeUnit unit) {
        final Long liveTime = Math.min(unit.toMillis(timeout), MAX_LIVE_TIME);
        int counter = 0;
        long submitted = 0;
        while (true) try {
            final long completed = uniqueMap.keySet().stream().filter(completeChecker).count();
            if ((getCompletedTaskCount() == getTaskCount() && getCompletedTaskCount() >= needProcessCount) || System.currentTimeMillis() - timeStart > liveTime) break;
            if (0 == ++counter % 100) {
                if (0 < needProcessCount)
                    logger.finest(String.format("Waiting for %s %d sec (%d of %d completed, %d tasks finished of %d submitted, %d in queue)", description, counter, completed,
                                                needProcessCount, getCompletedTaskCount(), getTaskCount(), getQueue().size()));

                if (submitted == getTaskCount() && 0 < getTaskCount() && submitted < needProcessCount) {
                    logger.severe(String.format("Nothing was submitted to %s, set needProcessCount to %d", description, submitted));
                    needProcessCount = submitted;
                }
                else submitted = getTaskCount();
            }
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            logger.severe("Wait interrupted for " + description);
        }

        if (0 < needProcessCount)
            logger.finest(String.format("Terminating working threads for %s after %s sec (%s of %s completed, %s tasks finished of %s submitted)", description, counter, uniqueMap.keySet()
                                                                                                                                                                                  .stream()
                                                                                                                                                                                  .filter(completeChecker).count(), needProcessCount, getCompletedTaskCount(), getTaskCount()));
        shutdownNow();

        uniqueMap.clear();
    }

    @Override
    public void execute(final Runnable command) {
        if (command instanceof IUniqueRunnable) {
            final T uniqueObj = ((IUniqueRunnable<T>) command).getUniqueObject();
            synchronized (uniqueObj) {
                if (null == uniqueMap.put(uniqueObj, (IUniqueRunnable<T>) command)) super.execute(command);
            }
        }
        else super.execute(command);
    }
}
