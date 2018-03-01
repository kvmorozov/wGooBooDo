package com.kmorozov.onedrive.tasks;

import com.kmorozov.onedrive.client.utils.LogUtils;
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
        startTime = System.currentTimeMillis();
    }

    public synchronized void same() {
        same++;
    }

    public synchronized void remoteDeleted() {
        remoteDeleted++;
    }

    public synchronized void localDeleted() {
        localDeleted++;
    }

    public synchronized void skipped() {
        skipped++;
    }

    public synchronized void error() {
        errors++;
    }

    public synchronized void fileUploaded(final boolean replace, final long size) {
        if (replace) {
            replaceUploaded++;
            replaceUploadedSize += size;
        } else {
            newUploaded++;
            newUploadedSize += size;
        }
    }

    public synchronized void fileDownloaded(final boolean replace, final long size) {
        if (replace) {
            replaceDownloaded++;
            replaceDownloadedSize += size;
        } else {
            newDownloaded++;
            newDownloadedSize += size;
        }
    }

    public synchronized void propertiesUpdated() {
        propsUpdated++;
    }

    public synchronized void report() {

        if (0 < errors) {
            log.error(String.format("%d tasks failed - see log for details", errors));
        }

        if (0 < same) {
            log.info(String.format("Skipped %d unchanged file%s", same, plural(same)));
        }

        if (0 < skipped) {
            log.info(String.format("Skipped %d ignored file%s", skipped, plural(skipped)));
        }

        if (0 < localDeleted) {
            log.info(String.format("Deleted %d local file%s", localDeleted, plural(skipped)));
        }

        if (0 < remoteDeleted) {
            log.info(String.format("Deleted %d remote file%s", remoteDeleted, plural(skipped)));
        }

        if (0 < propsUpdated) {
            log.info(String.format("Updated timestamps on %d file%s", propsUpdated, plural(skipped)));
        }

        if (0 < newUploaded || 0 < replaceUploaded) {

            final StringBuilder uploadedResult = new StringBuilder();

            uploadedResult.append(
                    String.format("Uploaded %d file%s (%s) - ",
                                  newUploaded + replaceUploaded,
                                  plural(newUploaded + replaceUploaded),
                                  LogUtils.readableFileSize(newUploadedSize + replaceUploadedSize)));

            if (0 < newUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                      newUploaded,
                                      plural(newUploaded),
                                      LogUtils.readableFileSize(newUploadedSize)));
            }

            if (0 < replaceUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                      replaceUploaded,
                                      plural(replaceUploaded),
                                      LogUtils.readableFileSize(replaceUploadedSize)));
            }

            log.info(uploadedResult.toString());
        }

        if (0 < newDownloaded || 0 < replaceDownloaded) {
            final StringBuilder downloadedResult = new StringBuilder();

            downloadedResult.append(
                    String.format("Downloaded %d file%s (%s) - ",
                                  newDownloaded + replaceDownloaded,
                                  plural(newDownloaded + replaceDownloaded),
                                  LogUtils.readableFileSize(newDownloadedSize + replaceDownloadedSize)));

            if (0 < newDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                      newDownloaded,
                                      plural(newDownloaded),
                                      LogUtils.readableFileSize(newDownloadedSize)));
            }

            if (0 < replaceDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                      replaceDownloaded,
                                      plural(replaceDownloaded),
                                      LogUtils.readableFileSize(replaceDownloadedSize)));
            }

            log.info(downloadedResult.toString());
        }

        final long elapsed = System.currentTimeMillis() - startTime;
        log.info(String.format("Elapsed time: %s", LogUtils.readableTime(elapsed)));
    }

    private static String plural(final long same) {
        return 1 == same ? "" : "s";
    }

    public void setTaskLogger(final Logger taskLogger) {
        this.taskLogger = taskLogger;
    }

    public void info(final String message) {
        if (null != taskLogger)
            taskLogger.info(message);
        else
            log.info(message);
    }

    public void warn(final String message) {
        if (null != taskLogger)
            taskLogger.warn(message);
        else
            log.warn(message);
    }
}
