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

    public DownloadTask(Task.TaskOptions options, File parent, OneDriveItem remoteFile, boolean replace, int chunkSize) {
        super(options);

        this.parent = Preconditions.checkNotNull(parent);
        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.replace = Preconditions.checkNotNull(replace);
        this.chunkSize = chunkSize;

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Specified parent is not a folder");
        }
    }

    public DownloadTask(Task.TaskOptions options, File parent, OneDriveItem remoteFile, boolean replace) {
        this(options, parent, remoteFile, replace, ResumableDownloader.MAXIMUM_CHUNK_SIZE);
    }

    public int priority() {
        return 50;
    }

    @Override
    public String toString() {
        return "Download " + this.remoteFile.getFullName();
    }

    @Override
    protected void taskBody() throws IOException {

        if (Task.isIgnored(this.remoteFile)) {
            this.reporter.skipped();
            return;
        }

        if (this.remoteFile.isDirectory()) {

            File newParent = this.fileSystem.createFolder(this.parent, this.remoteFile.getName());
            this.queue.add(new UpdatePropertiesTask(this.getTaskOptions(), this.remoteFile, newParent));

            for (OneDriveItem item : this.api.getChildren(this.remoteFile)) {
                this.queue.add(new DownloadTask(this.getTaskOptions(), newParent, item, false));
            }

        } else {

            if (Task.isSizeInvalid(this.remoteFile)) {
                this.reporter.skipped();
                return;
            }

            long startTime = System.currentTimeMillis();

            File downloadFile = null;

            try {
                downloadFile = this.fileSystem.createFile(this.parent, this.remoteFile.getName() + ".tmp");

                // The progress reporter
                ResumableDownloaderProgressListener progressListener = new ResumableDownloaderProgressListener() {

                    private long startTimeInner = System.currentTimeMillis();

                    @Override
                    public void progressChanged(ResumableDownloader downloader) {

                        switch (downloader.getDownloadState()) {
                            case MEDIA_IN_PROGRESS:
                                long elapsedTimeInner = System.currentTimeMillis() - this.startTimeInner;

                                DownloadTask.this.reporter.info(String.format("Downloaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                                            downloader.getProgress() * 100.0,
                                                            LogUtils.readableFileSize((long) downloader.getChunkSize()),
                                        0L < elapsedTimeInner ? LogUtils.readableFileSize((double) downloader.getChunkSize() / ((double) elapsedTimeInner / 1000.0d)) : 0,
                                        DownloadTask.this.remoteFile.getFullName()));

                                this.startTimeInner = System.currentTimeMillis();
                                break;
                            case MEDIA_COMPLETE:
                                long elapsedTime = System.currentTimeMillis() - startTime;
                                DownloadTask.this.reporter.info(String.format("Downloaded %s in %s (%s/s) of %s file %s",
                                                            LogUtils.readableFileSize(DownloadTask.this.remoteFile.getSize()),
                                                            LogUtils.readableTime(elapsedTime),
                                        0L < elapsedTime ? LogUtils.readableFileSize((double) DownloadTask.this.remoteFile.getSize() / ((double) elapsedTime / 1000.0d)) : 0,
                                        DownloadTask.this.replace ? "replaced" : "new",
                                        DownloadTask.this.remoteFile.getFullName()));
                        }
                    }
                };

                this.api.download(this.remoteFile, downloadFile, progressListener, this.chunkSize);

                // Do a CRC check on the downloaded file
                if (!this.fileSystem.verifyCrc(downloadFile, this.remoteFile.getCrc32())) {
                    throw new IOException(String.format("Download of file '%s' failed", this.remoteFile.getFullName()));
                }

                this.fileSystem.setAttributes(
                        downloadFile,
                        this.remoteFile.getCreatedDateTime(),
                        this.remoteFile.getLastModifiedDateTime());

                File localFile = new File(this.parent, this.remoteFile.getName());

                this.fileSystem.replaceFile(localFile, downloadFile);
                this.reporter.fileDownloaded(this.replace, this.remoteFile.getSize());
            } catch (Throwable e) {
                if (null != downloadFile) {
                    if (!downloadFile.delete()) {
                        this.reporter.warn("Unable to remove temporary file " + downloadFile.getPath());
                    }
                }

                throw e;
            }
        }
    }
}

