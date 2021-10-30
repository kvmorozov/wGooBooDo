package ru.kmorozov.onedrive;

import ru.kmorozov.onedrive.tasks.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueue {

    private final BlockingQueue<Task> queue = new PriorityBlockingQueue<>();
    private final Object suspendedMonitor = new Object();
    private final Object doneMonitor = new Object();
    private final AtomicInteger tasksInProgress = new AtomicInteger(0);
    private volatile boolean suspended;

    public void add(final Task t) {
        tasksInProgress.incrementAndGet();
        queue.add(t);
    }

    public Task take() throws InterruptedException {

        // Wait for the queue to be active
        synchronized (suspendedMonitor) {
            while (suspended) {
                suspendedMonitor.wait();
            }
        }

        return queue.take();
    }

    public void done(final Task t) {
        if (0 == tasksInProgress.decrementAndGet()) {
            synchronized (doneMonitor) {
                doneMonitor.notifyAll();
            }
        }
    }

    public void waitForCompletion() throws InterruptedException {
        while (0 < tasksInProgress.get()) {
            synchronized (doneMonitor) {
                doneMonitor.wait();
            }
        }
    }

    public void suspend(final int seconds) {

        synchronized (suspendedMonitor) {

            if (suspended) {
                return;
            }

            suspended = true;
        }

        try {
            Thread.sleep(seconds * 1000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            synchronized (suspendedMonitor) {
                suspended = false;
                suspendedMonitor.notifyAll();
            }
        }
    }
}
