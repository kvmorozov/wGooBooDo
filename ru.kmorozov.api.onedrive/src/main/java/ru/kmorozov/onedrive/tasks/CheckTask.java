package ru.kmorozov.onedrive.tasks;

import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider;
import ru.kmorozov.onedrive.CommandLineOpts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FileMatch;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CheckTask extends Task {

    private static final Logger log = LogManager.getLogger(UploadTask.class.getName());

    private final OneDriveItem remoteFile;
    private final File localFile;

    public CheckTask(Task.TaskOptions options, OneDriveItem remoteFile, File localFile) {
        super(options);
        this.remoteFile = Preconditions.checkNotNull(remoteFile);
        this.localFile = Preconditions.checkNotNull(localFile);
    }

    public int priority() {
        return 10;
    }

    @Override
    public String toString() {
        return String.format("Checking %s %s", this.remoteFile.isDirectory() ? "folder" : "file", this.remoteFile.getFullName());
    }

    @Override
    protected void taskBody() throws IOException {

        if (this.localFile.isDirectory() && this.remoteFile.isDirectory()) { // If we are syncing folders

            // Verify the timestamps
            FileMatch match = this.fileSystem.verifyMatch(
                    this.localFile,
                    this.remoteFile.getCreatedDateTime(),
                    this.remoteFile.getLastModifiedDateTime());

            if (FileMatch.NO == match) {
                this.queue.add(new UpdatePropertiesTask(this.getTaskOptions(), this.remoteFile, this.localFile));
            }

            OneDriveItem[] remoteFiles = this.api.getChildren(this.remoteFile);

            // Index the local files
            Map<String, File> localFileCache = Maps.newHashMap();

            File[] files = this.localFile.listFiles();
            if (null == files) {
                CheckTask.log.warn("Unable to recurse into local directory " + this.localFile.getPath());
                this.reporter.skipped();
                return;
            }

            for (File file : files) {
                localFileCache.put(file.getName(), file);
            }

            // Iterate over all the remote files
            for (OneDriveItem remoteFile : remoteFiles) {

                if (remoteFile.isDirectory() && !CommandLineOpts.getCommandLineOpts().isRecursive()) {
                    continue;
                }

                File localFile = localFileCache.remove(remoteFile.getName());
                this.processChild(remoteFile, localFile);
            }

            // Iterate over any local files we've not matched yet
            for (File localFile : localFileCache.values()) {

                if (localFile.isDirectory() && !CommandLineOpts.getCommandLineOpts().isRecursive()) {
                    continue;
                }

                this.processChild(null, localFile);
            }

            return;

        }

        // Skip if the file size is too big or if the file is ignored
        switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
            case UP:
                if (Task.isSizeInvalid(this.localFile) || Task.isIgnored(this.localFile)) {
                    this.reporter.skipped();
                    return;
                }
                break;
            case DOWN:
                if (Task.isSizeInvalid(this.remoteFile) || Task.isIgnored(this.remoteFile)) {
                    this.reporter.skipped();
                    return;
                }
                break;
        }

        if (this.localFile.isFile() && !this.remoteFile.isDirectory()) { // If we are syncing files

            // Check if the remote file matches the local file
            FileMatch match = this.fileSystem.verifyMatch(
                    this.localFile, this.remoteFile.getCrc32(),
                    this.remoteFile.getSize(),
                    this.remoteFile.getCreatedDateTime(),
                    this.remoteFile.getLastModifiedDateTime());

            switch (match) {
                case NO:
                    switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                        case UP:
                            this.queue.add(new UploadTask(this.getTaskOptions(), this.remoteFile.getParent(), this.localFile, true));
                            break;
                        case DOWN:
                            this.queue.add(new DownloadTask(this.getTaskOptions(), this.localFile.getParentFile(), this.remoteFile, true));
                            break;
                        default:
                            throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
                    }
                    break;
                case CRC:
                    this.queue.add(new UpdatePropertiesTask(this.getTaskOptions(), this.remoteFile, this.localFile));
                    break;
                case YES:
                    this.reporter.same();
                    break;
            }

        } else { // Resolve cases where remote and local disagree over whether the item is a file or folder
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    new DeleteTask(this.getTaskOptions(), this.remoteFile).taskBody(); // Execute immediately
                    this.queue.add(new UploadTask(this.getTaskOptions(), this.remoteFile.getParent(), this.localFile, true));
                    break;
                case DOWN:
                    new DeleteTask(this.getTaskOptions(), this.localFile).taskBody(); // Execute immediately
                    this.queue.add(new DownloadTask(this.getTaskOptions(), this.localFile.getParentFile(), this.remoteFile, true));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }
    }

    private void processChild(OneDriveItem remoteFile, File localFile) {

        if (null == remoteFile && null == localFile) {
            throw new IllegalArgumentException("Must specify at least one file");
        }

        if (null != remoteFile && Task.isIgnored(remoteFile) || null != localFile && Task.isIgnored((localFile))) {
            this.reporter.skipped();
            return;
        }

        boolean remoteOnly = null == localFile;
        boolean localOnly = null == remoteFile;

        // Case 1: We only have the file remotely
        if (remoteOnly) {
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    this.queue.add(new DeleteTask(this.getTaskOptions(), remoteFile));
                    break;
                case DOWN:
                    this.queue.add(new DownloadTask(this.getTaskOptions(), this.localFile, remoteFile, false));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }

        // Case 2: We only have the file locally
        else if (localOnly) {
            switch (CommandLineOpts.getCommandLineOpts().getDirection()) {
                case UP:
                    this.queue.add(new UploadTask(this.getTaskOptions(), this.remoteFile, localFile, false));
                    break;
                case DOWN:
                    this.queue.add(new DeleteTask(this.getTaskOptions(), localFile));
                    break;
                default:
                    throw new IllegalStateException("Unsupported direction " + CommandLineOpts.getCommandLineOpts().getDirection());
            }
        }

        // Case 3: We have the file in both locations
        else {
            this.queue.add(new CheckTask(this.getTaskOptions(), remoteFile, localFile));
        }
    }
}
