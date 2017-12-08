package com.wouterbreukink.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import com.wouterbreukink.onedrive.CommandLineOpts;
import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveUploadSession;
import com.wouterbreukink.onedrive.client.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class UploadTask extends Task {

    private static final Logger log = LogManager.getLogger(UploadTask.class.getName());

    private final OneDriveItem parent;
    private final File localFile;
    private final boolean replace;

    public UploadTask(final TaskOptions options, final OneDriveItem parent, final File localFile, final boolean replace) {

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
        return "Upload " + parent.getFullName() + localFile.getName();
    }

    @Override
    protected void taskBody() throws IOException {

        if (isIgnored(localFile)) {
            reporter.skipped();
            return;
        }

        if (localFile.isDirectory()) {
            final OneDriveItem newParent = api.createFolder(parent, localFile);

            for (final File f : localFile.listFiles()) {
                queue.add(new UploadTask(getTaskOptions(), newParent, f, false));
            }
        } else {

            if (isSizeInvalid(localFile)) {
                reporter.skipped();
                return;
            }

            final long startTime = System.currentTimeMillis();

            final OneDriveItem response;
            if (localFile.length() > CommandLineOpts.getCommandLineOpts().getSplitAfter() * 1024 * 1024) {

                int tryCount = 0;
                final OneDriveUploadSession session = api.startUploadSession(parent, localFile);

                while (!session.isComplete()) {
                    final long startTimeInner = System.currentTimeMillis();

                    try {
                        // We don't want to keep retrying infinitely
                        if (tryCount == CommandLineOpts.getCommandLineOpts().getTries()) {
                            break;
                        }

                        api.uploadChunk(session);

                        final long elapsedTimeInner = System.currentTimeMillis() - startTimeInner;

                        log.info(String.format("Uploaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                               ((double) session.getTotalUploaded() / session.getFile().length()) * 100,
                                               LogUtils.readableFileSize(session.getLastUploaded()),
                                0 < elapsedTimeInner ? LogUtils.readableFileSize(session.getLastUploaded() / (elapsedTimeInner / 1000d)) : 0,
                                               parent.getFullName() + localFile.getName()));

                        // After a successful upload we'll reset the tryCount
                        tryCount = 0;

                    } catch (final IOException ex) {
                        log.warn(String.format("Encountered '%s' while uploading chunk of %s for file %s",
                                               ex.getMessage(),
                                               LogUtils.readableFileSize(session.getLastUploaded()),
                                               parent.getFullName() + localFile.getName()));

                        tryCount++;
                    }
                }

                if (!session.isComplete()) {
                    throw new IOException(String.format("Gave up on multi-part upload after %s retries", CommandLineOpts.getCommandLineOpts().getTries()));
                }

                response = session.getItem();

            } else {
                response = replace ? api.replaceFile(parent, localFile) : api.uploadFile(parent, localFile);
            }

            final long elapsedTime = System.currentTimeMillis() - startTime;

            log.info(String.format("Uploaded %s in %s (%s/s) to %s file %s",
                                   LogUtils.readableFileSize(localFile.length()),
                                   LogUtils.readableTime(elapsedTime),
                    0 < elapsedTime ? LogUtils.readableFileSize(localFile.length() / (elapsedTime / 1000d)) : 0,
                                   replace ? "replace" : "new",
                                   response.getFullName()));

            reporter.fileUploaded(replace, localFile.length());
        }
    }
}

