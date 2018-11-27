package ru.kmorozov.onedrive;

import ru.kmorozov.onedrive.client.OneDriveAPIException;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.resources.Drive;
import ru.kmorozov.onedrive.client.utils.LogUtils;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider;
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FACTORY;
import ru.kmorozov.onedrive.tasks.CheckTask;
import ru.kmorozov.onedrive.tasks.Task;
import ru.kmorozov.onedrive.tasks.Task.TaskOptions;
import ru.kmorozov.onedrive.tasks.TaskReporter;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * OneDrive Java Client
 * Copyright (C) 2015 Wouter Breukink
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

        // Parse command line args
        try {
            CommandLineOpts.initialise(args);
        } catch (ParseException ex) {
            Main.log.error("Unable to parse command line arguments - " + ex.getMessage());
            CommandLineOpts.printHelp();
            return;
        }

        if (CommandLineOpts.getCommandLineOpts().help()) {
            CommandLineOpts.printHelp();
            return;
        }

        if (CommandLineOpts.getCommandLineOpts().version()) {
            String version = CommandLineOpts.getCommandLineOpts().getClass().getPackage().getImplementationVersion();
            Main.log.info("onedrive-java-client version " + (null != version ? version : "DEVELOPMENT"));
            return;
        }

        // Initialise a log file (if set)
        if (null != CommandLineOpts.getCommandLineOpts().getLogFile()) {
            String logFileName = LogUtils.addFileLogger(CommandLineOpts.getCommandLineOpts().getLogFile());
            Main.log.info(String.format("Writing log output to %s", logFileName));
        }

        if (CommandLineOpts.getCommandLineOpts().isAuthorise()) {
            AuthorisationProvider.FACTORY.printAuthInstructions("CLIENT_ID");
            return;
        }

        if (null == CommandLineOpts.getCommandLineOpts().getLocalPath()
                || null == CommandLineOpts.getCommandLineOpts().getRemotePath()
                || null == CommandLineOpts.getCommandLineOpts().getDirection()) {
            Main.log.error("Must specify --local, --remote and --direction");
            CommandLineOpts.printHelp();
            return;
        }

        // Initialise the OneDrive authorisation
        AuthorisationProvider authoriser;
        try {
            authoriser = AuthorisationProvider.FACTORY.create(CommandLineOpts.getCommandLineOpts().getKeyFile(), null, null);
            authoriser.getAccessToken();
        } catch (OneDriveAPIException ex) {
            Main.log.error("Unable to authorise client: " + ex.getMessage());
            Main.log.error("Re-run the application with --authorise");
            return;
        }

        // Initialise the providers
        OneDriveProvider api;
        FileSystemProvider fileSystem;
        if (CommandLineOpts.getCommandLineOpts().isDryRun()) {
            Main.log.warn("This is a dry run - no changes will be made");
            api = OneDriveProvider.FACTORY.readOnlyApi(authoriser);
            fileSystem = FACTORY.readOnlyProvider();
        } else {
            api = OneDriveProvider.FACTORY.readWriteApi(authoriser);
            fileSystem = FACTORY.readWriteProvider();
        }

        // Report on progress
        TaskReporter reporter = new TaskReporter();

        // Get the primary drive
        Drive primary = api.getDefaultDrive();

        // Report quotas
        Main.log.info(String.format("Using drive with id '%s' (%s). Usage %s of %s (%.2f%%)",
                               primary.getId(),
                               primary.getDriveType(),
                               LogUtils.readableFileSize(primary.getQuota().getUsed()),
                               LogUtils.readableFileSize(primary.getQuota().getTotal()),
                               ((double) primary.getQuota().getUsed() / (double) primary.getQuota().getTotal()) * 100.0));

        // Check the given root folder
        OneDriveItem rootFolder;
        try {
            rootFolder = api.getPath(CommandLineOpts.getCommandLineOpts().getRemotePath());
        } catch (OneDriveAPIException e) {
            if (404 == e.getCode()) {
                Main.log.error(String.format("Specified remote folder '%s' does not exist", CommandLineOpts.getCommandLineOpts().getRemotePath()));
            } else {
                Main.log.error(String.format("Unable to locate remote folder '%s' - %s", CommandLineOpts.getCommandLineOpts().getRemotePath(), e.getMessage()));
            }
            return;
        }

        if (null == rootFolder || !rootFolder.isDirectory()) {
            Main.log.error(String.format("Specified root '%s' is not a folder", CommandLineOpts.getCommandLineOpts().getRemotePath()));
            return;
        }

        // Check the target folder
        File localFolder = new File(CommandLineOpts.getCommandLineOpts().getLocalPath());

        if (!localFolder.exists() || !localFolder.isDirectory()) {
            Main.log.error(String.format("Specified local path '%s' is not a valid folder", CommandLineOpts.getCommandLineOpts().getLocalPath()));
            return;
        }

        Main.log.info(String.format("Starting at root folder '%s'", rootFolder.getFullName()));

        // Start synchronisation operation at the root
        TaskQueue queue = new TaskQueue();
        queue.add(new CheckTask(new TaskOptions(queue, api, fileSystem, reporter), rootFolder, localFolder));

        // Get a bunch of threads going
        ExecutorService executorService = Executors.newFixedThreadPool(CommandLineOpts.getCommandLineOpts().getThreads());

        for (int i = 0; i < CommandLineOpts.getCommandLineOpts().getThreads(); i++) {
            executorService.submit(() -> {
                try {
                    while (true) {
                        Task taskToRun = null;
                        try {
                            taskToRun = queue.take();
                            taskToRun.run();
                        } finally {
                            if (null != taskToRun) {
                                queue.done(taskToRun);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        queue.waitForCompletion();
        Main.log.info("Synchronisation complete");
        reporter.report();

        System.exit(0);
    }
}
