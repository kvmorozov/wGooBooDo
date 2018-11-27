package ru.kmorozov.onedrive.tasks;

import ru.kmorozov.onedrive.client.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskReporter {

    private static final Logger log = LogManager.getLogger(TaskReporter.class.getName());

    private int same;
    private int remoteDeleted;
    private int localDeleted;
    private int skipped;
    private int propsUpdated;
    private int errors;

    private int newUploaded;
    private long newUploadedSize;
    private int replaceUploaded;
    private long replaceUploadedSize;

    private int newDownloaded;
    private long newDownloadedSize;
    private int replaceDownloaded;
    private long replaceDownloadedSize;

    private Logger taskLogger;

    private final long startTime;

    public TaskReporter() {
        this.startTime = System.currentTimeMillis();
    }

    public synchronized void same() {
        this.same++;
    }

    public synchronized void remoteDeleted() {
        this.remoteDeleted++;
    }

    public synchronized void localDeleted() {
        this.localDeleted++;
    }

    public synchronized void skipped() {
        this.skipped++;
    }

    public synchronized void error() {
        this.errors++;
    }

    public synchronized void fileUploaded(boolean replace, long size) {
        if (replace) {
            this.replaceUploaded++;
            this.replaceUploadedSize += size;
        } else {
            this.newUploaded++;
            this.newUploadedSize += size;
        }
    }

    public synchronized void fileDownloaded(boolean replace, long size) {
        if (replace) {
            this.replaceDownloaded++;
            this.replaceDownloadedSize += size;
        } else {
            this.newDownloaded++;
            this.newDownloadedSize += size;
        }
    }

    public synchronized void propertiesUpdated() {
        this.propsUpdated++;
    }

    public synchronized void report() {

        if (0 < this.errors) {
            TaskReporter.log.error(String.format("%d tasks failed - see log for details", this.errors));
        }

        if (0 < this.same) {
            TaskReporter.log.info(String.format("Skipped %d unchanged file%s", this.same, TaskReporter.plural((long) this.same)));
        }

        if (0 < this.skipped) {
            TaskReporter.log.info(String.format("Skipped %d ignored file%s", this.skipped, TaskReporter.plural((long) this.skipped)));
        }

        if (0 < this.localDeleted) {
            TaskReporter.log.info(String.format("Deleted %d local file%s", this.localDeleted, TaskReporter.plural((long) this.skipped)));
        }

        if (0 < this.remoteDeleted) {
            TaskReporter.log.info(String.format("Deleted %d remote file%s", this.remoteDeleted, TaskReporter.plural((long) this.skipped)));
        }

        if (0 < this.propsUpdated) {
            TaskReporter.log.info(String.format("Updated timestamps on %d file%s", this.propsUpdated, TaskReporter.plural((long) this.skipped)));
        }

        if (0 < this.newUploaded || 0 < this.replaceUploaded) {

            StringBuilder uploadedResult = new StringBuilder();

            uploadedResult.append(
                    String.format("Uploaded %d file%s (%s) - ",
                            this.newUploaded + this.replaceUploaded,
                            TaskReporter.plural((long) (this.newUploaded + this.replaceUploaded)),
                                  LogUtils.readableFileSize(this.newUploadedSize + this.replaceUploadedSize)));

            if (0 < this.newUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                this.newUploaded,
                                TaskReporter.plural((long) this.newUploaded),
                                      LogUtils.readableFileSize(this.newUploadedSize)));
            }

            if (0 < this.replaceUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                this.replaceUploaded,
                                TaskReporter.plural((long) this.replaceUploaded),
                                      LogUtils.readableFileSize(this.replaceUploadedSize)));
            }

            TaskReporter.log.info(uploadedResult.toString());
        }

        if (0 < this.newDownloaded || 0 < this.replaceDownloaded) {
            StringBuilder downloadedResult = new StringBuilder();

            downloadedResult.append(
                    String.format("Downloaded %d file%s (%s) - ",
                            this.newDownloaded + this.replaceDownloaded,
                            TaskReporter.plural((long) (this.newDownloaded + this.replaceDownloaded)),
                                  LogUtils.readableFileSize(this.newDownloadedSize + this.replaceDownloadedSize)));

            if (0 < this.newDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                this.newDownloaded,
                                TaskReporter.plural((long) this.newDownloaded),
                                      LogUtils.readableFileSize(this.newDownloadedSize)));
            }

            if (0 < this.replaceDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                this.replaceDownloaded,
                                TaskReporter.plural((long) this.replaceDownloaded),
                                      LogUtils.readableFileSize(this.replaceDownloadedSize)));
            }

            TaskReporter.log.info(downloadedResult.toString());
        }

        long elapsed = System.currentTimeMillis() - this.startTime;
        TaskReporter.log.info(String.format("Elapsed time: %s", LogUtils.readableTime(elapsed)));
    }

    private static String plural(long same) {
        return 1L == same ? "" : "s";
    }

    public void setTaskLogger(Logger taskLogger) {
        this.taskLogger = taskLogger;
    }

    public void info(String message) {
        if (null != this.taskLogger)
            this.taskLogger.info(message);
        else
            TaskReporter.log.info(message);
    }

    public void warn(String message) {
        if (null != this.taskLogger)
            this.taskLogger.warn(message);
        else
            TaskReporter.log.warn(message);
    }
}
