package ru.kmorozov.onedrive.tasks

import com.google.api.client.util.Preconditions
import org.apache.logging.log4j.LogManager
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener
import ru.kmorozov.onedrive.client.utils.LogUtils
import java.io.File
import java.io.IOException

class DownloadTask @JvmOverloads constructor(options: TaskOptions, parent: File, remoteFile: OneDriveItem, replace: Boolean, private val chunkSize: Int = ResumableDownloader.MAXIMUM_CHUNK_SIZE) : Task(options) {
    private val parent: File
    private val remoteFile: OneDriveItem
    private val replace: Boolean

    init {

        this.parent = Preconditions.checkNotNull(parent)
        this.remoteFile = Preconditions.checkNotNull(remoteFile)
        this.replace = Preconditions.checkNotNull(replace)

        if (!parent.isDirectory) {
            throw IllegalArgumentException("Specified parent is not a folder")
        }
    }

    public override fun priority(): Int {
        return 50
    }

    override fun toString(): String {
        return "Download " + remoteFile.fullName
    }

    @Throws(IOException::class)
    override fun taskBody() {

        if (isIgnored(remoteFile)) {
            reporter.skipped()
            return
        }

        if (remoteFile.isDirectory) {

            val newParent = fileSystem.createFolder(parent, remoteFile.name)
            queue.add(UpdatePropertiesTask(taskOptions, remoteFile, newParent))

            for (item in api.getChildren(remoteFile)) {
                queue.add(DownloadTask(taskOptions, newParent, item, false))
            }

        } else {

            if (isSizeInvalid(remoteFile)) {
                reporter.skipped()
                return
            }

            val startTime = System.currentTimeMillis()

            var downloadFile: File? = null

            try {
                downloadFile = fileSystem.createFile(parent, remoteFile.name + ".tmp")

                // The progress reporter
                val progressListener = object : ResumableDownloaderProgressListener {

                    private var startTimeInner = System.currentTimeMillis()

                    override fun progressChanged(downloader: ResumableDownloader) {

                        when (downloader.downloadState) {
                            ResumableDownloader.DownloadState.MEDIA_IN_PROGRESS -> {
                                val elapsedTimeInner = System.currentTimeMillis() - startTimeInner

                                reporter.info(String.format("Downloaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                        downloader.progress * 100.0,
                                        LogUtils.readableFileSize(downloader.chunkSize.toLong()),
                                        if (0L < elapsedTimeInner) LogUtils.readableFileSize(downloader.chunkSize.toDouble() / (elapsedTimeInner.toDouble() / 1000.0)) else 0,
                                        remoteFile.fullName))

                                startTimeInner = System.currentTimeMillis()
                            }
                            ResumableDownloader.DownloadState.MEDIA_COMPLETE -> {
                                val elapsedTime = System.currentTimeMillis() - startTime
                                reporter.info(String.format("Downloaded %s in %s (%s/s) of %s file %s",
                                        LogUtils.readableFileSize(remoteFile.size),
                                        LogUtils.readableTime(elapsedTime),
                                        if (0L < elapsedTime) LogUtils.readableFileSize(remoteFile.size.toDouble() / (elapsedTime.toDouble() / 1000.0)) else 0,
                                        if (replace) "replaced" else "new",
                                        remoteFile.fullName))
                            }
                            ResumableDownloader.DownloadState.NOT_STARTED -> TODO()
                        }
                    }
                }

                api.download(remoteFile, downloadFile, progressListener, chunkSize)

                // Do a CRC check on the downloaded file
                if (!fileSystem.verifyCrc(downloadFile, remoteFile.crc32)) {
                    throw IOException(String.format("Download of file '%s' failed", remoteFile.fullName))
                }

                fileSystem.setAttributes(
                        downloadFile,
                        remoteFile.createdDateTime,
                        remoteFile.lastModifiedDateTime)

                val localFile = File(parent, remoteFile.name)

                fileSystem.replaceFile(localFile, downloadFile)
                reporter.fileDownloaded(replace, remoteFile.size)
            } catch (e: Throwable) {
                if (null != downloadFile) {
                    if (!downloadFile.delete()) {
                        reporter.warn("Unable to remove temporary file " + downloadFile.path)
                    }
                }

                throw e
            }

        }
    }

    companion object {

        private val log = LogManager.getLogger(DownloadTask::class.java.name)
    }
}

