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

    public void add(Task t) {
        this.tasksInProgress.incrementAndGet();
        this.queue.add(t);
    }

    public Task take() throws InterruptedException {

        // Wait for the queue to be active
        synchronized (this.suspendedMonitor) {
            while (this.suspended) {
                this.suspendedMonitor.wait();
            }
        }

        return this.queue.take();
    }

    public void done(Task t) {
        if (0 == this.tasksInProgress.decrementAndGet()) {
            synchronized (this.doneMonitor) {
                this.doneMonitor.notifyAll();
            }
        }
    }

    public void waitForCompletion() throws InterruptedException {
        while (0 < this.tasksInProgress.get()) {
            synchronized (this.doneMonitor) {
                this.doneMonitor.wait();
            }
        }
    }

    public void suspend(int seconds) {

        synchronized (this.suspendedMonitor) {

            if (this.suspended) {
                return;
            }

            this.suspended = true;
        }

        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            synchronized (this.suspendedMonitor) {
                this.suspended = false;
                this.suspendedMonitor.notifyAll();
            }
        }
    }
}
