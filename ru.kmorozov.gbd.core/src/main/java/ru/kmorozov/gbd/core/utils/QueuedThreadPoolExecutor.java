package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.extractors.IUniqueRunnable;

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

    private final int needProcessCount;
    private final long timeStart;

    public static final int THREAD_POOL_SIZE = 10;
    private static final int RETENTION_QUEUE_SIZE = 200;
    private static final long MAX_LIVE_TIME = 1 * 60 * 60 * 1000;

    private final Map<T, IUniqueRunnable<T>> uniqueMap = new ConcurrentHashMap<>();
    private final Predicate<T> completeChecker;

    public QueuedThreadPoolExecutor(int needProcessCount, int threadPoolSize, Predicate<T> completeChecker) {
        super(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(RETENTION_QUEUE_SIZE));
        this.needProcessCount = needProcessCount;
        this.timeStart = System.currentTimeMillis();
        this.completeChecker = completeChecker;

        setRejectedExecutionHandler((r, executor) -> {
            try {
                getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(long timeout, TimeUnit unit) {
        while (uniqueMap.keySet().stream().filter(completeChecker).count() < needProcessCount && System.currentTimeMillis() - timeStart < MAX_LIVE_TIME)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        shutdown();
        try {
            awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        uniqueMap.clear();
    }

    @Override
    public void execute(Runnable command) {
        if (command instanceof IUniqueRunnable) synchronized (this) {
            T uniqueObj = (T) ((IUniqueRunnable) command).getUniqueObject();
            if (uniqueMap.put(uniqueObj, (IUniqueRunnable<T>) command) == null) super.execute(command);
        }
        else super.execute(command);
    }
}
