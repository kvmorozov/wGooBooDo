package ru.kmorozov.onedrive

import ru.kmorozov.onedrive.client.OneDriveAPIException
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.resources.Drive
import ru.kmorozov.onedrive.client.utils.LogUtils
import ru.kmorozov.onedrive.filesystem.FileSystemProvider
import ru.kmorozov.onedrive.tasks.CheckTask
import ru.kmorozov.onedrive.tasks.Task
import ru.kmorozov.onedrive.tasks.TaskReporter
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/***
 * OneDrive Java Client
 * Copyright (C) 2015 Wouter Breukink
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
object Main {

    private val log = LogManager.getLogger(Main::class.java.name)

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // Parse command line args
        try {
            CommandLineOpts.initialise(args)
        } catch (ex: ParseException) {
            log.error("Unable to parse command line arguments - " + ex.message)
            CommandLineOpts.printHelp()
            return
        }

        if (CommandLineOpts.commandLineOpts.help()) {
            CommandLineOpts.printHelp()
            return
        }

        if (CommandLineOpts.commandLineOpts.version()) {
            val version = CommandLineOpts.commandLineOpts.javaClass.getPackage().implementationVersion
            log.info("onedrive-java-client version " + if (null != version) version else "DEVELOPMENT")
            return
        }

        // Initialise a log file (if set)
        if (null != CommandLineOpts.commandLineOpts.logFile) {
            val logFileName = LogUtils.addFileLogger(CommandLineOpts.commandLineOpts.logFile!!)
            log.info("Writing log output to $logFileName")
        }

        if (CommandLineOpts.commandLineOpts.isAuthorise) {
            AuthorisationProvider.FACTORY.printAuthInstructions("CLIENT_ID")
            return
        }

        if (null == CommandLineOpts.commandLineOpts.localPath
                || null == CommandLineOpts.commandLineOpts.remotePath
                || null == CommandLineOpts.commandLineOpts.direction) {
            log.error("Must specify --local, --remote and --direction")
            CommandLineOpts.printHelp()
            return
        }

        // Initialise the OneDrive authorisation
        val authoriser: AuthorisationProvider
        try {
            authoriser = AuthorisationProvider.FACTORY.create(CommandLineOpts.commandLineOpts.keyFile, "", "")
            authoriser.accessToken
        } catch (ex: OneDriveAPIException) {
            log.error("Unable to authorise client: " + ex.message)
            log.error("Re-run the application with --authorise")
            return
        }

        // Initialise the providers
        val api: OneDriveProvider
        val fileSystem: FileSystemProvider
        if (CommandLineOpts.commandLineOpts.isDryRun) {
            log.warn("This is a dry run - no changes will be made")
            api = OneDriveProvider.FACTORY.readOnlyApi(authoriser)
            fileSystem = FileSystemProvider.FACTORY.readOnlyProvider()
        } else {
            api = OneDriveProvider.FACTORY.readWriteApi(authoriser)
            fileSystem = FileSystemProvider.FACTORY.readWriteProvider()
        }

        // Report on progress
        val reporter = TaskReporter()

        // Get the primary drive
        val primary = api.defaultDrive

        // Report quotas
        log.info(String.format("Using drive with id '%s' (%s). Usage %s of %s (%.2f%%)",
                primary.id,
                primary.driveType,
                LogUtils.readableFileSize(primary.quota!!.used),
                LogUtils.readableFileSize(primary.quota.total),
                primary.quota.used.toDouble() / primary.quota.total.toDouble() * 100.0))

        // Check the given root folder
        val rootFolder: OneDriveItem?
        try {
            rootFolder = api.getPath(CommandLineOpts.commandLineOpts.remotePath!!)
        } catch (e: OneDriveAPIException) {
            if (404 == e.code) {
                log.error("Specified remote folder '${CommandLineOpts.commandLineOpts.remotePath}' does not exist")
            } else {
                log.error("Unable to locate remote folder '${CommandLineOpts.commandLineOpts.remotePath}' - ${e.message}")
            }
            return
        }

        if (null == rootFolder || !rootFolder.isDirectory) {
            log.error("Specified root '${CommandLineOpts.commandLineOpts.remotePath}' is not a folder")
            return
        }

        // Check the target folder
        val localFolder = File(CommandLineOpts.commandLineOpts.localPath!!)

        if (!localFolder.exists() || !localFolder.isDirectory) {
            log.error("Specified local path '${CommandLineOpts.commandLineOpts.localPath}' is not a valid folder")
            return
        }

        log.info("Starting at root folder '${rootFolder.fullName}'")

        // Start synchronisation operation at the root
        val queue = TaskQueue()
        queue.add(CheckTask(Task.TaskOptions(queue, api, fileSystem, reporter), rootFolder, localFolder))

        // Get a bunch of threads going
        val executorService = Executors.newFixedThreadPool(CommandLineOpts.commandLineOpts.threads)

        for (i in 0 until CommandLineOpts.commandLineOpts.threads) {
            executorService.submit {
                try {
                    while (true) {
                        var taskToRun: Task? = null
                        try {
                            taskToRun = queue.take()
                            taskToRun!!.run()
                        } finally {
                            if (null != taskToRun) {
                                queue.done(taskToRun)
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }

        queue.waitForCompletion()
        log.info("Synchronisation complete")
        reporter.report()

        System.exit(0)
    }
}
