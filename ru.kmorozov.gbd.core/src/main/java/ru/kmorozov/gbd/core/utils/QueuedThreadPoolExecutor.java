package ru.kmorozov.gbd.core.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 12.11.2016.
 */
public class QueuedThreadPoolExecutor extends ThreadPoolExecutor {

    private final int needProcessCount;
    private final long timeStart;

    public static final int THREAD_POOL_SIZE = 10;
    private static final int RETENTION_QUEUE_SIZE = 200;
    private static final long MAX_LIVE_TIME = 1 * 60 * 60 * 1000;

    public QueuedThreadPoolExecutor(int needProcessCount, int threadPoolSize) {
        super(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(RETENTION_QUEUE_SIZE));
        this.needProcessCount = needProcessCount;
        this.timeStart = System.currentTimeMillis();

        setRejectedExecutionHandler((r, executor) -> {
            try {
                getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(long timeout, TimeUnit unit) {
        while (getCompletedTaskCount() < needProcessCount && System.currentTimeMillis() - timeStart < MAX_LIVE_TIME)
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
    }
}
