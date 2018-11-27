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

    protected Task(final TaskOptions options) {
        this.queue = Preconditions.checkNotNull(options.getQueue());
        this.api = Preconditions.checkNotNull(options.getApi());
        this.fileSystem = Preconditions.checkNotNull(options.getFileSystem());
        this.reporter = Preconditions.checkNotNull(options.getReporter());

        reporter.setTaskLogger(log);

        this.id = taskIdCounter.getAndIncrement();
        this.attempt = 0;
    }

    protected static boolean isSizeInvalid(final File localFile) {
        return isSizeInvalid(localFile.getPath(), localFile.length());
    }

    protected static boolean isSizeInvalid(final OneDriveItem remoteFile) {
        return isSizeInvalid(remoteFile.getFullName(), remoteFile.getSize());
    }

    private static boolean isSizeInvalid(final String filename, final long size) {
        final int maxSizeKb = CommandLineOpts.getCommandLineOpts().getMaxSizeKb();
        if (0 < maxSizeKb && size > (long) (maxSizeKb * 1024)) {
            log.debug(String.format("Skipping file %s - size is %dKB (bigger than maximum of %dKB)",
                    filename,
                    size / 1024L,
                    maxSizeKb));
            return true;
        }

        return false;
    }

    protected static boolean isIgnored(final OneDriveItem remoteFile) {
        final boolean ignored = isIgnored(remoteFile.getName() + (remoteFile.isDirectory() ? File.separator : ""));

        if (ignored) {
            log.debug(String.format("Skipping ignored remote file %s", remoteFile.getFullName()));
        }

        return ignored;
    }

    protected static boolean isIgnored(final File localFile) {
        final boolean ignored = isIgnored(localFile.getName() + (localFile.isDirectory() ? File.separator : ""));

        if (ignored) {
            log.debug(String.format("Skipping ignored local file %s", localFile.getPath()));
        }

        return ignored;
    }

    private static boolean isIgnored(final String name) {
        final Set<String> ignoredSet = CommandLineOpts.getCommandLineOpts().getIgnored();
        return null != ignoredSet && ignoredSet.contains(name);
    }

    protected TaskOptions getTaskOptions() {
        return new TaskOptions(queue, api, fileSystem, reporter);
    }

    protected abstract int priority();

    protected abstract void taskBody() throws IOException;

    protected String getId() {
        return this.id + ":" + this.attempt;
    }

    public void run() {
        attempt++;
        try {
            log.debug(String.format("Starting task %d:%d - %s", id, attempt, this.toString()));
            taskBody();
            return;
        } catch (final HttpResponseException ex) {

            switch (ex.getStatusCode()) {
                case 401:
                    log.warn(String.format("Task %s encountered %s", getId(), ex.getMessage()));
                    break;
                case 500:
                case 502:
                case 503:
                case 504:
                    log.warn(String.format("Task %s encountered %s - sleeping 10 seconds", getId(), ex.getMessage()));
                    queue.suspend(10);
                    break;
                case 429:
                case 509:
                    log.warn(String.format("Task %s encountered %s - sleeping 60 seconds", getId(), ex.getMessage()));
                    queue.suspend(60);
                    break;
                default:
                    log.warn(String.format("Task %s encountered %s", getId(), ex.getMessage()));
            }
        } catch (final Exception ex) {
            log.error(String.format("Task %s encountered exception", getId()), ex);
            queue.suspend(1);
        }

        if (attempt < CommandLineOpts.getCommandLineOpts().getTries()) {
            queue.add(this);
        } else {
            reporter.error();
            log.error(String.format("Task %d did not complete - %s", id, this.toString()));
        }
    }

    public int compareTo(final Task o) {
        return o.priority() - priority();
    }

    public static class TaskOptions {

        private final TaskQueue queue;
        private final OneDriveProvider api;
        private final FileSystemProvider fileSystem;
        private final TaskReporter reporter;

        public TaskOptions(final TaskQueue queue, final OneDriveProvider api, final FileSystemProvider fileSystem, final TaskReporter reporter) {
            this.queue = queue;
            this.api = api;
            this.fileSystem = fileSystem;
            this.reporter = reporter;
        }

        public TaskQueue getQueue() {
            return queue;
        }

        public OneDriveProvider getApi() {
            return api;
        }

        public FileSystemProvider getFileSystem() {
            return fileSystem;
        }

        public TaskReporter getReporter() {
            return reporter;
        }
    }
}
