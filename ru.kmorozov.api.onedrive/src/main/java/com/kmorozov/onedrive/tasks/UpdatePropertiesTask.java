package com.kmorozov.onedrive.tasks;

import com.google.api.client.util.Preconditions;
import com.kmorozov.onedrive.client.OneDriveItem;
import com.kmorozov.onedrive.CommandLineOpts;
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

    public UpdatePropertiesTask(final TaskOptions options, final OneDriveItem remoteFile, final File localFile) {

        super(options);

        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.localFile = Preconditions.checkNotNull(localFile);
    }

    public int priority() {
        return 50;
    }

    @Override
    public String toString() {
        return "Update properties for " + remoteFile.getFullName();
    }

    @Override
    protected void taskBody() throws IOException {

        switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
            case UP:
                final BasicFileAttributes attr = Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
                // Timestamp rounded to the nearest second
                final Date localCreatedDate = new Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000);
                final Date localModifiedDate = new Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000);

                api.updateFile(remoteFile, localCreatedDate, localModifiedDate);

                log.info("Updated remote timestamps for item " + remoteFile.getFullName());

                break;
            case DOWN:
                fileSystem.setAttributes(localFile, remoteFile.getCreatedDateTime(), remoteFile.getLastModifiedDateTime());
                log.info("Updated local timestamps for item " + remoteFile.getFullName());
                break;
            default:
                throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
        }

        reporter.propertiesUpdated();
    }
}

