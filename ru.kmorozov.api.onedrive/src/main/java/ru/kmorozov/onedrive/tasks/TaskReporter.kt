package ru.kmorozov.onedrive.tasks

import ru.kmorozov.onedrive.client.utils.LogUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

open class TaskReporter {

    private var same: Int = 0
    private var remoteDeleted: Int = 0
    private var localDeleted: Int = 0
    private var skipped: Int = 0
    private var propsUpdated: Int = 0
    private var errors: Int = 0

    private var newUploaded: Int = 0
    private var newUploadedSize: Long = 0
    private var replaceUploaded: Int = 0
    private var replaceUploadedSize: Long = 0

    private var newDownloaded: Int = 0
    private var newDownloadedSize: Long = 0
    private var replaceDownloaded: Int = 0
    private var replaceDownloadedSize: Long = 0

    private var taskLogger: Logger? = null

    private val startTime: Long

    init {
        startTime = System.currentTimeMillis()
    }

    @Synchronized
    fun same() {
        same++
    }

    @Synchronized
    fun remoteDeleted() {
        remoteDeleted++
    }

    @Synchronized
    fun localDeleted() {
        localDeleted++
    }

    @Synchronized
    fun skipped() {
        skipped++
    }

    @Synchronized
    fun error() {
        errors++
    }

    @Synchronized
    fun fileUploaded(replace: Boolean, size: Long) {
        if (replace) {
            replaceUploaded++
            replaceUploadedSize += size
        } else {
            newUploaded++
            newUploadedSize += size
        }
    }

    @Synchronized
    fun fileDownloaded(replace: Boolean, size: Long) {
        if (replace) {
            replaceDownloaded++
            replaceDownloadedSize += size
        } else {
            newDownloaded++
            newDownloadedSize += size
        }
    }

    @Synchronized
    fun propertiesUpdated() {
        propsUpdated++
    }

    @Synchronized
    fun report() {

        if (0 < errors) {
            log.error(String.format("%d tasks failed - see log for details", errors))
        }

        if (0 < same) {
            log.info(String.format("Skipped %d unchanged file%s", same, plural(same.toLong())))
        }

        if (0 < skipped) {
            log.info(String.format("Skipped %d ignored file%s", skipped, plural(skipped.toLong())))
        }

        if (0 < localDeleted) {
            log.info(String.format("Deleted %d local file%s", localDeleted, plural(skipped.toLong())))
        }

        if (0 < remoteDeleted) {
            log.info(String.format("Deleted %d remote file%s", remoteDeleted, plural(skipped.toLong())))
        }

        if (0 < propsUpdated) {
            log.info(String.format("Updated timestamps on %d file%s", propsUpdated, plural(skipped.toLong())))
        }

        if (0 < newUploaded || 0 < replaceUploaded) {

            val uploadedResult = StringBuilder()

            uploadedResult.append(
                    String.format("Uploaded %d file%s (%s) - ",
                            newUploaded + replaceUploaded,
                            plural((newUploaded + replaceUploaded).toLong()),
                            LogUtils.readableFileSize(newUploadedSize + replaceUploadedSize)))

            if (0 < newUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                newUploaded,
                                plural(newUploaded.toLong()),
                                LogUtils.readableFileSize(newUploadedSize)))
            }

            if (0 < replaceUploaded) {
                uploadedResult.append(
                        String.format("%d new file%s (%s) ",
                                replaceUploaded,
                                plural(replaceUploaded.toLong()),
                                LogUtils.readableFileSize(replaceUploadedSize)))
            }

            log.info(uploadedResult.toString())
        }

        if (0 < newDownloaded || 0 < replaceDownloaded) {
            val downloadedResult = StringBuilder()

            downloadedResult.append(
                    String.format("Downloaded %d file%s (%s) - ",
                            newDownloaded + replaceDownloaded,
                            plural((newDownloaded + replaceDownloaded).toLong()),
                            LogUtils.readableFileSize(newDownloadedSize + replaceDownloadedSize)))

            if (0 < newDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                newDownloaded,
                                plural(newDownloaded.toLong()),
                                LogUtils.readableFileSize(newDownloadedSize)))
            }

            if (0 < replaceDownloaded) {
                downloadedResult.append(
                        String.format("%d new file%s (%s) ",
                                replaceDownloaded,
                                plural(replaceDownloaded.toLong()),
                                LogUtils.readableFileSize(replaceDownloadedSize)))
            }

            log.info(downloadedResult.toString())
        }

        val elapsed = System.currentTimeMillis() - startTime
        log.info(String.format("Elapsed time: %s", LogUtils.readableTime(elapsed)))
    }

    fun setTaskLogger(taskLogger: Logger) {
        this.taskLogger = taskLogger
    }

    open fun info(message: String) {
        if (null != taskLogger)
            taskLogger!!.info(message)
        else
            log.info(message)
    }

    open fun warn(message: String) {
        if (null != taskLogger)
            taskLogger!!.warn(message)
        else
            log.warn(message)
    }

    companion object {

        private val log = LogManager.getLogger(TaskReporter::class.java.name)

        private fun plural(same: Long): String {
            return if (1L == same) "" else "s"
        }
    }
}
