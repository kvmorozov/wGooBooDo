package ru.kmorozov.onedrive.tasks

import com.google.api.client.util.Preconditions
import org.apache.logging.log4j.LogManager
import ru.kmorozov.onedrive.client.OneDriveItem
import java.io.File
import java.io.IOException

class DeleteTask : Task {
    private val remoteFile: OneDriveItem?
    private val localFile: File?

    constructor(options: TaskOptions, remoteFile: OneDriveItem) : super(options) {

        this.remoteFile = Preconditions.checkNotNull(remoteFile)
        this.localFile = null
    }

    constructor(options: TaskOptions, localFile: File) : super(options) {

        this.localFile = Preconditions.checkNotNull(localFile)
        this.remoteFile = null
    }

    public override fun priority(): Int {
        return 100
    }

    override fun toString(): String {
        return if (null != localFile) {
            "Delete local file " + localFile.path
        } else {
            "Delete remote file " + remoteFile!!.fullName
        }
    }

    @Throws(IOException::class)
    public override fun taskBody() {
        if (null != localFile) {
            fileSystem.delete(localFile)
            reporter.localDeleted()
            log.info("Deleted local file " + localFile.path)
        } else {
            if (remoteFile != null) {
                api.delete(remoteFile)
                reporter.remoteDeleted()
                log.info("Deleted remote file " + remoteFile.fullName)
            }
        }
    }

    companion object {

        private val log = LogManager.getLogger(DeleteTask::class.java.name)
    }
}
