package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class DeleteTask extends Task {

    private static final Logger log = LogManager.getLogger(DeleteTask.class.getName());
    private final OneDriveItem remoteFile;
    private final File localFile;

    public DeleteTask(Task.TaskOptions options, OneDriveItem remoteFile) {

        super(options);

        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        localFile = null;
    }

    public DeleteTask(Task.TaskOptions options, File localFile) {

        super(options);

        this.localFile = Preconditions.checkNotNull(localFile);
        remoteFile = null;
    }

    public int priority() {
        return 100;
    }

    @Override
    public String toString() {
        if (null != this.localFile) {
            return "Delete local file " + this.localFile.getPath();
        } else {
            return "Delete remote file " + this.remoteFile.getFullName();
        }
    }

    @Override
    protected void taskBody() throws IOException {
        if (null != this.localFile) {
            this.fileSystem.delete(this.localFile);
            this.reporter.localDeleted();
            DeleteTask.log.info("Deleted local file " + this.localFile.getPath());
        } else {
            this.api.delete(this.remoteFile);
            this.reporter.remoteDeleted();
            DeleteTask.log.info("Deleted remote file " + this.remoteFile.getFullName());
        }
    }
}
