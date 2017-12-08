package com.wouterbreukink.onedrive.tasks;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.wouterbreukink.onedrive.CommandLineOpts;
import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.filesystem.FileSystemProvider;
import com.wouterbreukink.onedrive.filesystem.FileSystemProvider.FileMatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CheckTask extends Task {

    private static final Logger log = LogManager.getLogger(UploadTask.class.getName());

    private final OneDriveItem remoteFile;
    private final File localFile;

    public CheckTask(final TaskOptions options, final OneDriveItem remoteFile, final File localFile) {
        super(options);
        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.localFile = Preconditions.checkNotNull(localFile);
    }

    public int priority() {
        return 10;
    }

    @Override
    public String toString() {
        return String.format("Checking %s %s", remoteFile.isDirectory() ? "folder" : "file", remoteFile.getFullName());
    }

    @Override
    protected void taskBody() throws IOException {

        if (localFile.isDirectory() && remoteFile.isDirectory()) { // If we are syncing folders

            // Verify the timestamps
            final FileMatch match = fileSystem.verifyMatch(
                    localFile,
                    remoteFile.getCreatedDateTime(),
                    remoteFile.getLastModifiedDateTime());

            if (FileSystemProvider.FileMatch.NO == match) {
                queue.add(new UpdatePropertiesTask(getTaskOptions(), remoteFile, localFile));
            }

            final OneDriveItem[] remoteFiles = api.getChildren(remoteFile);

            // Index the local files
            final Map<String, File> localFileCache = Maps.newHashMap();

            final File[] files = localFile.listFiles();
            if (null == files) {
                log.warn("Unable to recurse into local directory " + localFile.getPath());
                reporter.skipped();
                return;
            }

            for (final File file : files) {
                localFileCache.put(file.getName(), file);
            }

            // Iterate over all the remote files
            for (final OneDriveItem remoteFile : remoteFiles) {

                if (remoteFile.isDirectory() && !CommandLineOpts.getCommandLineOpts().isRecursive()) {
                    continue;
                }

                final File localFile = localFileCache.remove(remoteFile.getName());
                processChild(remoteFile, localFile);
            }

            // Iterate over any local files we've not matched yet
            for (final File localFile : localFileCache.values()) {

                if (localFile.isDirectory() && !CommandLineOpts.getCommandLineOpts().isRecursive()) {
                    continue;
                }

                processChild(null, localFile);
            }

            return;

        }

        // Skip if the file size is too big or if the file is ignored
        switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
            case UP:
                if (isSizeInvalid(localFile) || isIgnored(localFile)) {
                    reporter.skipped();
                    return;
                }
                break;
            case DOWN:
                if (isSizeInvalid(remoteFile) || isIgnored(remoteFile)) {
                    reporter.skipped();
                    return;
                }
                break;
        }

        if (localFile.isFile() && !remoteFile.isDirectory()) { // If we are syncing files

            // Check if the remote file matches the local file
            final FileMatch match = fileSystem.verifyMatch(
                    localFile, remoteFile.getCrc32(),
                    remoteFile.getSize(),
                    remoteFile.getCreatedDateTime(),
                    remoteFile.getLastModifiedDateTime());

            switch (match) {
                case NO:
                    switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                        case UP:
                            queue.add(new UploadTask(getTaskOptions(), remoteFile.getParent(), localFile, true));
                            break;
                        case DOWN:
                            queue.add(new DownloadTask(getTaskOptions(), localFile.getParentFile(), remoteFile, true));
                            break;
                        default:
                            throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
                    }
                    break;
                case CRC:
                    queue.add(new UpdatePropertiesTask(getTaskOptions(), remoteFile, localFile));
                    break;
                case YES:
                    reporter.same();
                    break;
            }

        } else { // Resolve cases where remote and local disagree over whether the item is a file or folder
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    new DeleteTask(getTaskOptions(), remoteFile).taskBody(); // Execute immediately
                    queue.add(new UploadTask(getTaskOptions(), remoteFile.getParent(), localFile, true));
                    break;
                case DOWN:
                    new DeleteTask(getTaskOptions(), localFile).taskBody(); // Execute immediately
                    queue.add(new DownloadTask(getTaskOptions(), localFile.getParentFile(), remoteFile, true));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }
    }

    private void processChild(final OneDriveItem remoteFile, final File localFile) {

        if (null == remoteFile && null == localFile) {
            throw new IllegalArgumentException("Must specify at least one file");
        }

        if (null != remoteFile && isIgnored(remoteFile) || null != localFile && isIgnored((localFile))) {
            reporter.skipped();
            return;
        }

        final boolean remoteOnly = null == localFile;
        final boolean localOnly = null == remoteFile;

        // Case 1: We only have the file remotely
        if (remoteOnly) {
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    queue.add(new DeleteTask(getTaskOptions(), remoteFile));
                    break;
                case DOWN:
                    queue.add(new DownloadTask(getTaskOptions(), this.localFile, remoteFile, false));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }

        // Case 2: We only have the file locally
        else if (localOnly) {
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    queue.add(new UploadTask(getTaskOptions(), this.remoteFile, localFile, false));
                    break;
                case DOWN:
                    queue.add(new DeleteTask(getTaskOptions(), localFile));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }

        // Case 3: We have the file in both locations
        else {
            queue.add(new CheckTask(getTaskOptions(), remoteFile, localFile));
        }
    }
}
