package ru.kmorozov.onedrive.tasks

import com.google.api.client.util.Preconditions
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.CommandLineOpts
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.kmorozov.onedrive.tasks.Task.TaskOptions

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date
import java.util.concurrent.TimeUnit

class UpdatePropertiesTask(options: TaskOptions, remoteFile: OneDriveItem, localFile: File) : Task(options) {
    private val remoteFile: OneDriveItem
    private val localFile: File

    init {

        this.remoteFile = Preconditions.checkNotNull(remoteFile)
        this.localFile = Preconditions.checkNotNull(localFile)
    }

    public override fun priority(): Int {
        return 50
    }

    override fun toString(): String {
        return "Update properties for " + remoteFile.fullName
    }

    @Throws(IOException::class)
    override fun taskBody() {

        when (CommandLineOpts.commandLineOpts.direction) {
            CommandLineOpts.Direction.UP -> {
                val attr = Files.readAttributes(localFile.toPath(), BasicFileAttributes::class.java)
                // Timestamp rounded to the nearest second
                val localCreatedDate = Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L)
                val localModifiedDate = Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L)

                api.updateFile(remoteFile, localCreatedDate, localModifiedDate)

                log.info("Updated remote timestamps for item " + remoteFile.fullName)
            }
            CommandLineOpts.Direction.DOWN -> {
                fileSystem.setAttributes(localFile, remoteFile.createdDateTime, remoteFile.lastModifiedDateTime)
                log.info("Updated local timestamps for item " + remoteFile.fullName)
            }
            else -> throw IllegalStateException("Unsupported direction " + CommandLineOpts.commandLineOpts.direction!!)
        }

        reporter.propertiesUpdated()
    }

    companion object {

        private val log = LogManager.getLogger(UpdatePropertiesTask::class.java.name)
    }
}

