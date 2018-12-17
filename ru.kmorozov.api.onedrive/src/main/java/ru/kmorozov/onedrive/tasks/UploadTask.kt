package ru.kmorozov.onedrive.tasks

import com.google.api.client.util.Preconditions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.kmorozov.onedrive.CommandLineOpts
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveUploadSession
import ru.kmorozov.onedrive.client.utils.LogUtils
import ru.kmorozov.onedrive.tasks.Task.TaskOptions

import java.io.File
import java.io.IOException

class UploadTask(options: TaskOptions, parent: OneDriveItem, localFile: File, private val replace: Boolean) : Task(options) {

    private val parent: OneDriveItem
    private val localFile: File

    init {

        this.parent = Preconditions.checkNotNull(parent)
        this.localFile = Preconditions.checkNotNull(localFile)

        if (!parent.isDirectory) {
            throw IllegalArgumentException("Specified parent is not a folder")
        }
    }

    public override fun priority(): Int {
        return 50
    }

    override fun toString(): String {
        return "Upload " + parent.fullName + localFile.name
    }

    @Throws(IOException::class)
    override fun taskBody() {

        if (isIgnored(localFile)) {
            reporter.skipped()
            return
        }

        if (localFile.isDirectory) {
            val newParent = api.createFolder(parent, localFile.name)

            for (f in localFile.listFiles()!!) {
                queue.add(UploadTask(taskOptions, newParent, f, false))
            }
        } else {

            if (isSizeInvalid(localFile)) {
                reporter.skipped()
                return
            }

            val startTime = System.currentTimeMillis()

            val response: OneDriveItem?
            if (localFile.length() > (CommandLineOpts.commandLineOpts.splitAfter * 1024 * 1024).toLong()) {

                var tryCount = 0
                val session = api.startUploadSession(parent, localFile)

                while (!session.isComplete) {
                    val startTimeInner = System.currentTimeMillis()

                    try {
                        // We don't want to keep retrying infinitely
                        if (tryCount == CommandLineOpts.commandLineOpts.tries) {
                            break
                        }

                        api.uploadChunk(session)

                        val elapsedTimeInner = System.currentTimeMillis() - startTimeInner

                        log.info(String.format("Uploaded chunk (progress %.1f%%) of %s (%s/s) for file %s",
                                session.totalUploaded.toDouble() / session.file.length().toDouble() * 100.0,
                                LogUtils.readableFileSize(session.lastUploaded),
                                if (0L < elapsedTimeInner) LogUtils.readableFileSize(session.lastUploaded.toDouble() / (elapsedTimeInner.toDouble() / 1000.0)) else 0,
                                parent.fullName + localFile.name))

                        // After a successful upload we'll reset the tryCount
                        tryCount = 0

                    } catch (ex: IOException) {
                        log.warn(String.format("Encountered '%s' while uploading chunk of %s for file %s",
                                ex.message,
                                LogUtils.readableFileSize(session.lastUploaded),
                                parent.fullName + localFile.name))

                        tryCount++
                    }

                }

                if (!session.isComplete) {
                    throw IOException(String.format("Gave up on multi-part upload after %s retries", CommandLineOpts.commandLineOpts.tries))
                }

                response = session.item

            } else {
                response = if (replace) api.replaceFile(parent, localFile) else api.uploadFile(parent, localFile)
            }

            val elapsedTime = System.currentTimeMillis() - startTime

            log.info(String.format("Uploaded %s in %s (%s/s) to %s file %s",
                    LogUtils.readableFileSize(localFile.length()),
                    LogUtils.readableTime(elapsedTime),
                    if (0L < elapsedTime) LogUtils.readableFileSize(localFile.length().toDouble() / (elapsedTime.toDouble() / 1000.0)) else 0,
                    if (replace) "replace" else "new",
                    response!!.fullName))

            reporter.fileUploaded(replace, localFile.length())
        }
    }

    companion object {

        private val log = LogManager.getLogger(UploadTask::class.java.name)
    }
}

