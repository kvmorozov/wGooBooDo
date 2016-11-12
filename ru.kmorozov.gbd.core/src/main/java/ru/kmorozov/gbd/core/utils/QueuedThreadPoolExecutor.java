package ru.kmorozov.gbd.core.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 12.11.2016.
 */
public class QueuedThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int THREAD_POOL_SIZE = 10;
    private static final int RETENTION_QUEUE_SIZE = 200;

    public QueuedThreadPoolExecutor() {
        this(THREAD_POOL_SIZE);
    }

    public QueuedThreadPoolExecutor(int threadPoolSize) {
        super(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(RETENTION_QUEUE_SIZE));

        setRejectedExecutionHandler((r, executor) -> {
            try {
                getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void terminate(long timeout, TimeUnit unit) {
        shutdown();
        try {
            awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
