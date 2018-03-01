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

    public DeleteTask(final TaskOptions options, final OneDriveItem remoteFile) {

        super(options);

        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.localFile = null;
    }

    public DeleteTask(final TaskOptions options, final File localFile) {

        super(options);

        this.localFile = Preconditions.checkNotNull(localFile);
        this.remoteFile = null;
    }

    public int priority() {
        return 100;
    }

    @Override
    public String toString() {
        if (null != localFile) {
            return "Delete local file " + localFile.getPath();
        } else {
            return "Delete remote file " + remoteFile.getFullName();
        }
    }

    @Override
    protected void taskBody() throws IOException {
        if (null != localFile) {
            fileSystem.delete(localFile);
            reporter.localDeleted();
            log.info("Deleted local file " + localFile.getPath());
        } else {
            api.delete(remoteFile);
            reporter.remoteDeleted();
            log.info("Deleted remote file " + remoteFile.getFullName());
        }
    }
}
