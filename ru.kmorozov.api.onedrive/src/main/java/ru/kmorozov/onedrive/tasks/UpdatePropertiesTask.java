package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.CommandLineOpts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UpdatePropertiesTask extends Task {

    private static final Logger log = LogManager.getLogger(UpdatePropertiesTask.class.getName());
    private final OneDriveItem remoteFile;
    private final File localFile;

    public UpdatePropertiesTask(Task.TaskOptions options, OneDriveItem remoteFile, File localFile) {

        super(options);

        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.localFile = Preconditions.checkNotNull(localFile);
    }

    public int priority() {
        return 50;
    }

    @Override
    public String toString() {
        return "Update properties for " + this.remoteFile.getFullName();
    }

    @Override
    protected void taskBody() throws IOException {

        switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
            case UP:
                BasicFileAttributes attr = Files.readAttributes(this.localFile.toPath(), BasicFileAttributes.class);
                // Timestamp rounded to the nearest second
                Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L);
                Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L);

                this.api.updateFile(this.remoteFile, localCreatedDate, localModifiedDate);

                UpdatePropertiesTask.log.info("Updated remote timestamps for item " + this.remoteFile.getFullName());

                break;
            case DOWN:
                this.fileSystem.setAttributes(this.localFile, this.remoteFile.getCreatedDateTime(), this.remoteFile.getLastModifiedDateTime());
                UpdatePropertiesTask.log.info("Updated local timestamps for item " + this.remoteFile.getFullName());
                break;
            default:
                throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
        }

        this.reporter.propertiesUpdated();
    }
}

