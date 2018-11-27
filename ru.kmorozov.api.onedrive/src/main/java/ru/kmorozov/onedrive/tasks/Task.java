package ru.kmorozov.onedrive.tasks;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.Preconditions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.CommandLineOpts;
import ru.kmorozov.onedrive.TaskQueue;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Task implements Runnable, Comparable<Task> {

    private static final Logger log = LogManager.getLogger(Task.class.getName());
    private static final AtomicInteger taskIdCounter = new AtomicInteger(1);
    protected final TaskQueue queue;
    protected final OneDriveProvider api;
    protected final FileSystemProvider fileSystem;
    protected final TaskReporter reporter;

    private final int id;
    private int attempt;

    protected Task(Task.TaskOptions options) {
        queue = Preconditions.checkNotNull(options.getQueue());
        api = Preconditions.checkNotNull(options.getApi());
        fileSystem = Preconditions.checkNotNull(options.getFileSystem());
        reporter = Preconditions.checkNotNull(options.getReporter());

        this.reporter.setTaskLogger(Task.log);

        id = Task.taskIdCounter.getAndIncrement();
        attempt = 0;
    }

    protected static boolean isSizeInvalid(File localFile) {
        return Task.isSizeInvalid(localFile.getPath(), localFile.length());
    }

    protected static boolean isSizeInvalid(OneDriveItem remoteFile) {
        return Task.isSizeInvalid(remoteFile.getFullName(), remoteFile.getSize());
    }

    private static boolean isSizeInvalid(String filename, long size) {
        int maxSizeKb = CommandLineOpts.getCommandLineOpts().getMaxSizeKb();
        if (0 < maxSizeKb && size > (long) (maxSizeKb * 1024)) {
            Task.log.debug(String.format("Skipping file %s - size is %dKB (bigger than maximum of %dKB)",
                    filename,
                    size / 1024L,
                    maxSizeKb));
            return true;
        }

        return false;
    }

    protected static boolean isIgnored(OneDriveItem remoteFile) {
        boolean ignored = Task.isIgnored(remoteFile.getName() + (remoteFile.isDirectory() ? File.separator : ""));

        if (ignored) {
            Task.log.debug(String.format("Skipping ignored remote file %s", remoteFile.getFullName()));
        }

        return ignored;
    }

    protected static boolean isIgnored(File localFile) {
        boolean ignored = Task.isIgnored(localFile.getName() + (localFile.isDirectory() ? File.separator : ""));

        if (ignored) {
            Task.log.debug(String.format("Skipping ignored local file %s", localFile.getPath()));
        }

        return ignored;
    }

    private static boolean isIgnored(String name) {
        Set<String> ignoredSet = CommandLineOpts.getCommandLineOpts().getIgnored();
        return null != ignoredSet && ignoredSet.contains(name);
    }

    protected Task.TaskOptions getTaskOptions() {
        return new Task.TaskOptions(this.queue, this.api, this.fileSystem, this.reporter);
    }

    protected abstract int priority();

    protected abstract void taskBody() throws IOException;

    protected String getId() {
        return id + ":" + attempt;
    }

    public void run() {
        this.attempt++;
        try {
            Task.log.debug(String.format("Starting task %d:%d - %s", this.id, this.attempt, toString()));
            this.taskBody();
            return;
        } catch (HttpResponseException ex) {

            switch (ex.getStatusCode()) {
                case 401:
                    Task.log.warn(String.format("Task %s encountered %s", this.getId(), ex.getMessage()));
                    break;
                case 500:
                case 502:
                case 503:
                case 504:
                    Task.log.warn(String.format("Task %s encountered %s - sleeping 10 seconds", this.getId(), ex.getMessage()));
                    this.queue.suspend(10);
                    break;
                case 429:
                case 509:
                    Task.log.warn(String.format("Task %s encountered %s - sleeping 60 seconds", this.getId(), ex.getMessage()));
                    this.queue.suspend(60);
                    break;
                default:
                    Task.log.warn(String.format("Task %s encountered %s", this.getId(), ex.getMessage()));
            }
        } catch (Exception ex) {
            Task.log.error(String.format("Task %s encountered exception", this.getId()), ex);
            this.queue.suspend(1);
        }

        if (this.attempt < CommandLineOpts.getCommandLineOpts().getTries()) {
            this.queue.add(this);
        } else {
            this.reporter.error();
            Task.log.error(String.format("Task %d did not complete - %s", this.id, toString()));
        }
    }

    public int compareTo(Task o) {
        return o.priority() - this.priority();
    }

    public static class TaskOptions {

        private final TaskQueue queue;
        private final OneDriveProvider api;
        private final FileSystemProvider fileSystem;
        private final TaskReporter reporter;

        public TaskOptions(TaskQueue queue, OneDriveProvider api, FileSystemProvider fileSystem, TaskReporter reporter) {
            this.queue = queue;
            this.api = api;
            this.fileSystem = fileSystem;
            this.reporter = reporter;
        }

        public TaskQueue getQueue() {
            return this.queue;
        }

        public OneDriveProvider getApi() {
            return this.api;
        }

        public FileSystemProvider getFileSystem() {
            return this.fileSystem;
        }

        public TaskReporter getReporter() {
            return this.reporter;
        }
    }
}
