package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kmorozov.onedrive.CommandLineOpts;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveUploadSession;
import ru.kmorozov.onedrive.client.utils.LogUtils;

import java.io.File;
import java.io.IOException;

public class UploadTask extends Task {

    private static final Logger log = LogManager.getLogger(UploadTask.class.getName());

    private final OneDriveItem parent;
    private final File localFile;
    private final boolean replace;

    public UploadTask(Task.TaskOptions options, OneDriveItem parent, File localFile, boolean replace) {

        super(options);

        this.parent = Preconditions.checkNotNull(parent);
        this.localFile = Preconditions.checkNotNull(localFile);
        this.replace = replace;

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Specified parent is not a folder");
        }
    }

    public int priority() {
        return 50;
    }

    @Override
    public String toString() {
        return "Upload " + this.parent.getFullName() + this.localFile.getName();
    }

    @Override
    protected void taskBody() throws IOException {

        if (Task.isIgnored(this.localFile)) {
            this.reporter.skipped();
            return;
        }

        if (this.localFile.isDirectory()) {
            OneDriveItem newParent = this.api.createFolder(this.parent, this.localFile.getName());

            for (File f : this.localFile.listFiles()) {
                this.queue.add(new UploadTask(this.getTaskOptions(), newParent, f, false));
            }
        } else {

            if (Task.isSizeInvalid(this.localFile)) {
                this.reporter.skipped();
                return;
            }

            long startTime = System.currentTimeMillis();

            OneDriveItem response;
            if (this.localFile.length() > (long) (CommandLineOpts.getCommandLineOpts().getSplitAfter() * 1024 * 1024)) {

                int tryCount = 0;
                OneDriveUploadSession session = this.api.startUploadSession(this.parent, this.localFile);

                while (!session.isComplete()) {
                    long startTimeInner = System.currentTimeMillis();

                    try {
                        // We don't want to keep retrying infinitely
                        if (tryCount == CommandLineOpts.getCommandLineOpts().getTries()) {
                            break;
                        }

                        this.api.uploadChunk(session);

                        long elapsedTimeInner = System.currentTimeMillis() - startTimeInner;

                        UploadTask.log.info(String.format("Uploaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                ((double) session.getTotalUploaded() / (double) session.getFile().length()) * 100.0,
                                LogUtils.readableFileSize(session.getLastUploaded()),
                                0L < elapsedTimeInner ? LogUtils.readableFileSize((double) session.getLastUploaded() / ((double) elapsedTimeInner / 1000.0d)) : 0,
                                this.parent.getFullName() + this.localFile.getName()));

                        // After a successful upload we'll reset the tryCount
                        tryCount = 0;

                    } catch (IOException ex) {
                        UploadTask.log.warn(String.format("Encountered '%s' while uploading chunk of %s for file %s",
                                ex.getMessage(),
                                LogUtils.readableFileSize(session.getLastUploaded()),
                                this.parent.getFullName() + this.localFile.getName()));

                        tryCount++;
                    }
                }

                if (!session.isComplete()) {
                    throw new IOException(String.format("Gave up on multi-part upload after %s retries", CommandLineOpts.getCommandLineOpts().getTries()));
                }

                response = session.getItem();

            } else {
                response = this.replace ? this.api.replaceFile(this.parent, this.localFile) : this.api.uploadFile(this.parent, this.localFile);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            UploadTask.log.info(String.format("Uploaded %s in %s (%s/s) to %s file %s",
                    LogUtils.readableFileSize(this.localFile.length()),
                    LogUtils.readableTime(elapsedTime),
                    0L < elapsedTime ? LogUtils.readableFileSize((double) this.localFile.length() / ((double) elapsedTime / 1000.0d)) : 0,
                    this.replace ? "replace" : "new",
                    response.getFullName()));

            this.reporter.fileUploaded(this.replace, this.localFile.length());
        }
    }
}

