package ru.kmorozov.onedrive.tasks

import com.google.api.client.util.Maps
import com.google.api.client.util.Preconditions
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.filesystem.FileSystemProvider
import ru.kmorozov.onedrive.CommandLineOpts
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.kmorozov.onedrive.tasks.Task.TaskOptions

import java.io.File
import java.io.IOException

class CheckTask(options: TaskOptions, remoteFile: OneDriveItem, localFile: File) : Task(options) {

    private val remoteFile: OneDriveItem
    private val localFile: File

    init {
        this.remoteFile = Preconditions.checkNotNull(remoteFile)
        this.localFile = Preconditions.checkNotNull(localFile)
    }

    public override fun priority(): Int {
        return 10
    }

    override fun toString(): String {
        return "Checking ${if (remoteFile.isDirectory) "folder" else "file"} ${remoteFile.fullName}"
    }

    @Throws(IOException::class)
    override fun taskBody() {

        if (localFile.isDirectory && remoteFile.isDirectory) { // If we are syncing folders

            // Verify the timestamps
            val match = fileSystem.verifyMatch(
                    localFile,
                    remoteFile.createdDateTime,
                    remoteFile.lastModifiedDateTime)

            if (FileSystemProvider.FileMatch.NO == match) {
                queue.add(UpdatePropertiesTask(taskOptions, remoteFile, localFile))
            }

            val remoteFiles = api.getChildren(remoteFile)

            // Index the local files
            val localFileCache = Maps.newHashMap<String, File>()

            val files = localFile.listFiles()
            if (null == files) {
                log.warn("Unable to recurse into local directory " + localFile.path)
                reporter.skipped()
                return
            }

            for (file in files) {
                localFileCache[file.name] = file
            }

            // Iterate over all the remote files
            for (remoteFile in remoteFiles) {

                if (remoteFile.isDirectory && !CommandLineOpts.commandLineOpts.isRecursive) {
                    continue
                }

                val localFile = localFileCache.remove(remoteFile.name)
                processChild(remoteFile, localFile)
            }

            // Iterate over any local files we've not matched yet
            for (localFile in localFileCache.values) {

                if (localFile.isDirectory && !CommandLineOpts.commandLineOpts.isRecursive) {
                    continue
                }

                processChild(null, localFile)
            }

            return

        }

        // Skip if the file size is too big or if the file is ignored
        when (CommandLineOpts.commandLineOpts.direction) {
            CommandLineOpts.Direction.UP -> if (isSizeInvalid(localFile) || isIgnored(localFile)) {
                reporter.skipped()
                return
            }
            CommandLineOpts.Direction.DOWN -> if (isSizeInvalid(remoteFile) || isIgnored(remoteFile)) {
                reporter.skipped()
                return
            }
        }

        if (localFile.isFile && !remoteFile.isDirectory) { // If we are syncing files

            // Check if the remote file matches the local file
            val match = fileSystem.verifyMatch(
                    localFile, remoteFile.crc32,
                    remoteFile.size,
                    remoteFile.createdDateTime,
                    remoteFile.lastModifiedDateTime)

            when (match) {
                FileSystemProvider.FileMatch.NO -> when (CommandLineOpts.commandLineOpts.direction) {
                    CommandLineOpts.Direction.UP -> queue.add(UploadTask(taskOptions, remoteFile.parent!!, localFile, true))
                    CommandLineOpts.Direction.DOWN -> queue.add(DownloadTask(taskOptions, localFile.parentFile, remoteFile, true))
                    else -> throw IllegalStateException("Unsupported direction " + CommandLineOpts.commandLineOpts.direction!!)
                }
                FileSystemProvider.FileMatch.CRC -> queue.add(UpdatePropertiesTask(taskOptions, remoteFile, localFile))
                FileSystemProvider.FileMatch.YES -> reporter.same()
            }

        } else { // Resolve cases where remote and local disagree over whether the item is a file or folder
            when (CommandLineOpts.commandLineOpts.direction) {
                CommandLineOpts.Direction.UP -> {
                    DeleteTask(taskOptions, remoteFile).taskBody() // Execute immediately
                    queue.add(UploadTask(taskOptions, remoteFile.parent!!, localFile, true))
                }
                CommandLineOpts.Direction.DOWN -> {
                    DeleteTask(taskOptions, localFile).taskBody() // Execute immediately
                    queue.add(DownloadTask(taskOptions, localFile.parentFile, remoteFile, true))
                }
                else -> throw IllegalStateException("Unsupported direction " + CommandLineOpts.commandLineOpts.direction!!)
            }
        }
    }

    private fun processChild(remoteFile: OneDriveItem?, localFile: File?) {

        if (null == remoteFile && null == localFile) {
            throw IllegalArgumentException("Must specify at least one file")
        }

        if (null != remoteFile && isIgnored(remoteFile) || null != localFile && isIgnored(localFile)) {
            reporter.skipped()
            return
        }

        val remoteOnly = null == localFile
        val localOnly = null == remoteFile

        // Case 1: We only have the file remotely
        if (remoteOnly) {
            when (CommandLineOpts.commandLineOpts.direction) {
                CommandLineOpts.Direction.UP -> queue.add(DeleteTask(taskOptions, remoteFile!!))
                CommandLineOpts.Direction.DOWN -> queue.add(DownloadTask(taskOptions, this.localFile, remoteFile!!, false))
                else -> throw IllegalStateException("Unsupported direction " + CommandLineOpts.commandLineOpts.direction!!)
            }
        } else if (localOnly) {
            when (CommandLineOpts.commandLineOpts.direction) {
                CommandLineOpts.Direction.UP -> queue.add(UploadTask(taskOptions, this.remoteFile, localFile!!, false))
                CommandLineOpts.Direction.DOWN -> queue.add(DeleteTask(taskOptions, localFile!!))
                else -> throw IllegalStateException("Unsupported direction " + CommandLineOpts.commandLineOpts.direction!!)
            }
        } else {
            queue.add(CheckTask(taskOptions, remoteFile!!, localFile!!))
        }// Case 3: We have the file in both locations
        // Case 2: We only have the file locally
    }

    companion object {

        private val log = LogManager.getLogger(UploadTask::class.java.name)
    }
}
