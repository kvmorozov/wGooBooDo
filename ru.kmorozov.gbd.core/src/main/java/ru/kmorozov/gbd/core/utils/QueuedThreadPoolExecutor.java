package ru.kmorozov.gbd.core.utils;

import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
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

    private final static Logger logger = ExecutionContext.INSTANCE.getLogger("Executor");

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
        Long liveTime = Math.min(unit.toMillis(timeout), MAX_LIVE_TIME);
        while (uniqueMap.keySet().stream().filter(completeChecker).count() < needProcessCount && System.currentTimeMillis() - timeStart < liveTime)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        shutdownNow();

        uniqueMap.clear();
    }

    @Override
    public void execute(final Runnable command) {
        if (command instanceof IUniqueRunnable) synchronized (this) {
            T uniqueObj = (T) ((IUniqueRunnable) command).getUniqueObject();
            if (uniqueMap.put(uniqueObj, (IUniqueRunnable<T>) command) == null) super.execute(new Thread(command) {

                @Override
                public void interrupt() {
                    logger.severe("Interrupted thread " + command.toString());
                    super.interrupt();
                }
            });
        }
        else super.execute(command);
    }
}
