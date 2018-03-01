package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.utils.LogUtils;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class DownloadTask extends Task {

    private static final Logger log = LogManager.getLogger(DownloadTask.class.getName());
    private final File parent;
    private final OneDriveItem remoteFile;
    private final boolean replace;
    private final int chunkSize;

    public DownloadTask(final TaskOptions options, final File parent, final OneDriveItem remoteFile, final boolean replace, final int chunkSize) {
        super(options);

        this.parent = Preconditions.checkNotNull(parent);
        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.replace = Preconditions.checkNotNull(replace);
        this.chunkSize = chunkSize;

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Specified parent is not a folder");
        }
    }

    public DownloadTask(final TaskOptions options, final File parent, final OneDriveItem remoteFile, final boolean replace) {
        this(options, parent, remoteFile, replace, ResumableDownloader.MAXIMUM_CHUNK_SIZE);
    }

    public int priority() {
        return 50;
    }

    @Override
    public String toString() {
        return "Download " + remoteFile.getFullName();
    }

    @Override
    protected void taskBody() throws IOException {

        if (isIgnored(remoteFile)) {
            reporter.skipped();
            return;
        }

        if (remoteFile.isDirectory()) {

            final File newParent = fileSystem.createFolder(parent, remoteFile.getName());
            queue.add(new UpdatePropertiesTask(getTaskOptions(), remoteFile, newParent));

            for (final OneDriveItem item : api.getChildren(remoteFile)) {
                queue.add(new DownloadTask(getTaskOptions(), newParent, item, false));
            }

        } else {

            if (isSizeInvalid(remoteFile)) {
                reporter.skipped();
                return;
            }

            final long startTime = System.currentTimeMillis();

            File downloadFile = null;

            try {
                downloadFile = fileSystem.createFile(parent, remoteFile.getName() + ".tmp");

                // The progress reporter
                final ResumableDownloaderProgressListener progressListener = new ResumableDownloaderProgressListener() {

                    private long startTimeInner = System.currentTimeMillis();

                    @Override
                    public void progressChanged(final ResumableDownloader downloader) {

                        switch (downloader.getDownloadState()) {
                            case MEDIA_IN_PROGRESS:
                                final long elapsedTimeInner = System.currentTimeMillis() - startTimeInner;

                                reporter.info(String.format("Downloaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                                            downloader.getProgress() * 100,
                                                            LogUtils.readableFileSize(downloader.getChunkSize()),
                                        0 < elapsedTimeInner ? LogUtils.readableFileSize(downloader.getChunkSize() / (elapsedTimeInner / 1000d)) : 0,
                                                            remoteFile.getFullName()));

                                startTimeInner = System.currentTimeMillis();
                                break;
                            case MEDIA_COMPLETE:
                                final long elapsedTime = System.currentTimeMillis() - startTime;
                                reporter.info(String.format("Downloaded %s in %s (%s/s) of %s file %s",
                                                            LogUtils.readableFileSize(remoteFile.getSize()),
                                                            LogUtils.readableTime(elapsedTime),
                                        0 < elapsedTime ? LogUtils.readableFileSize(remoteFile.getSize() / (elapsedTime / 1000d)) : 0,
                                                            replace ? "replaced" : "new",
                                                            remoteFile.getFullName()));
                        }
                    }
                };

                api.download(remoteFile, downloadFile, progressListener, chunkSize);

                // Do a CRC check on the downloaded file
                if (!fileSystem.verifyCrc(downloadFile, remoteFile.getCrc32())) {
                    throw new IOException(String.format("Download of file '%s' failed", remoteFile.getFullName()));
                }

                fileSystem.setAttributes(
                        downloadFile,
                        remoteFile.getCreatedDateTime(),
                        remoteFile.getLastModifiedDateTime());

                final File localFile = new File(parent, remoteFile.getName());

                fileSystem.replaceFile(localFile, downloadFile);
                reporter.fileDownloaded(replace, remoteFile.getSize());
            } catch (final Throwable e) {
                if (null != downloadFile) {
                    if (!downloadFile.delete()) {
                        reporter.warn("Unable to remove temporary file " + downloadFile.getPath());
                    }
                }

                throw e;
            }
        }
    }
}

