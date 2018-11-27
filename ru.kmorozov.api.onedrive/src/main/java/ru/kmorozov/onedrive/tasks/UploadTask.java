package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kmorozov.onedrive.CommandLineOpts;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveUploadSession;
import ru.kmorozov.onedrive.client.utils.LogUtils;
import ru.kmorozov.onedrive.tasks.Task.TaskOptions;

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

        if (Task.isIgnored(localFile)) {
            reporter.skipped();
            return;
        }

        if (localFile.isDirectory()) {
            final OneDriveItem newParent = api.createFolder(parent, localFile.getName());

            for (final File f : localFile.listFiles()) {
                queue.add(new UploadTask(getTaskOptions(), newParent, f, false));
            }
        } else {

            if (Task.isSizeInvalid(localFile)) {
                reporter.skipped();
                return;
            }

            final long startTime = System.currentTimeMillis();

            final OneDriveItem response;
            if (localFile.length() > (long) (CommandLineOpts.getCommandLineOpts().getSplitAfter() * 1024 * 1024)) {

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
                                ((double) session.getTotalUploaded() / (double) session.getFile().length()) * 100.0,
                                LogUtils.readableFileSize(session.getLastUploaded()),
                                0L < elapsedTimeInner ? LogUtils.readableFileSize((double) session.getLastUploaded() / ((double) elapsedTimeInner / 1000.0d)) : 0,
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
                    0L < elapsedTime ? LogUtils.readableFileSize((double) localFile.length() / ((double) elapsedTime / 1000.0d)) : 0,
                    replace ? "replace" : "new",
                    response.getFullName()));

            reporter.fileUploaded(replace, localFile.length());
        }
    }
}

