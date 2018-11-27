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
    private static final long MAX_LIVE_TIME = TimeUnit.HOURS.toMillis(1L);
    private final long timeStart;
    private final Map<T, IUniqueRunnable<T>> uniqueMap = new ConcurrentHashMap<>();
    private final Predicate<T> completeChecker;
    private long needProcessCount;
    private final String description;

    public QueuedThreadPoolExecutor(long needProcessCount, int threadPoolSize, Predicate<T> completeChecker, String description) {
        super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(QueuedThreadPoolExecutor.RETENTION_QUEUE_SIZE));
        this.needProcessCount = needProcessCount;
        timeStart = System.currentTimeMillis();
        this.completeChecker = completeChecker;
        this.description = description;

        this.setRejectedExecutionHandler((r, executor) -> {
            try {
                if (r instanceof IUniqueRunnable) {
                    synchronized (((IUniqueRunnable<T>) r).getUniqueObject()) {
                        if (!completeChecker.test(((IUniqueRunnable<T>) r).getUniqueObject())) this.getQueue().put(r);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(long timeout, TimeUnit unit) {
        Long liveTime = Math.min(unit.toMillis(timeout), QueuedThreadPoolExecutor.MAX_LIVE_TIME);
        int counter = 0;
        long submitted = 0L;
        while (true) try {
            long completed = this.uniqueMap.keySet().stream().filter(this.completeChecker).count();
            if ((this.getCompletedTaskCount() == this.getTaskCount() && this.getCompletedTaskCount() >= this.needProcessCount) || System.currentTimeMillis() - this.timeStart > liveTime)
                break;
            if (0 == ++counter % 100) {
                if (0L < this.needProcessCount)
                    QueuedThreadPoolExecutor.logger.finest(String.format("Waiting for %s %d sec (%d of %d completed, %d tasks finished of %d submitted, %d in queue)", this.description, counter, completed,
                            this.needProcessCount, this.getCompletedTaskCount(), this.getTaskCount(), this.getQueue().size()));

                if (submitted == this.getTaskCount() && 0L < this.getTaskCount() && submitted < this.needProcessCount) {
                    QueuedThreadPoolExecutor.logger.severe(String.format("Nothing was submitted to %s, set needProcessCount to %d", this.description, submitted));
                    this.needProcessCount = submitted;
                } else submitted = this.getTaskCount();
            }
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            QueuedThreadPoolExecutor.logger.severe("Wait interrupted for " + this.description);
        }

        if (0L < this.needProcessCount)
            QueuedThreadPoolExecutor.logger.finest(String.format("Terminating working threads for %s after %s sec (%s of %s completed, %s tasks finished of %s submitted)", this.description, counter, this.uniqueMap.keySet()
                    .stream()
                    .filter(this.completeChecker).count(), this.needProcessCount, this.getCompletedTaskCount(), this.getTaskCount()));
        this.shutdownNow();

        this.uniqueMap.clear();
    }

    @Override
    public void execute(Runnable command) {
        if (command == null)
            return;

        if (command instanceof IUniqueRunnable) {
            T uniqueObj = ((IUniqueRunnable<T>) command).getUniqueObject();
            synchronized (uniqueObj) {
                if (null == this.uniqueMap.put(uniqueObj, (IUniqueRunnable<T>) command)) super.execute(command);
            }
        } else super.execute(command);
    }
}
