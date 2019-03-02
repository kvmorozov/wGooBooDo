package ru.kmorozov.onedrive.tasks

import com.google.api.client.http.HttpResponseException
import com.google.api.client.util.Preconditions
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.CommandLineOpts
import ru.kmorozov.onedrive.TaskQueue
import ru.kmorozov.onedrive.filesystem.FileSystemProvider
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

abstract class Task protected constructor(options: TaskOptions) : Runnable, Comparable<Task> {
    protected val queue: TaskQueue
    protected val api: OneDriveProvider
    protected val fileSystem: FileSystemProvider
    protected val reporter: TaskReporter

    private val id: Int
    private var attempt: Int = 0

    protected val taskOptions: TaskOptions
        get() = TaskOptions(queue, api, fileSystem, reporter)

    init {
        this.queue = Preconditions.checkNotNull(options.queue)
        this.api = Preconditions.checkNotNull(options.api)
        this.fileSystem = Preconditions.checkNotNull(options.fileSystem)
        this.reporter = Preconditions.checkNotNull(options.reporter)

        reporter.setTaskLogger(log)

        this.id = taskIdCounter.getAndIncrement()
        this.attempt = 0
    }

    protected abstract fun priority(): Int

    @Throws(IOException::class)
    protected abstract fun taskBody()

    protected fun getId(): String {
        return this.id.toString() + ":" + this.attempt
    }

    override fun run() {
        attempt++
        try {
            log.debug(String.format("Starting task %d:%d - %s", id, attempt, this.toString()))
            taskBody()
            return
        } catch (ex: HttpResponseException) {

            when (ex.statusCode) {
                401 -> log.warn("Task ${getId()} encountered ${ex.message}")
                500, 502, 503, 504 -> {
                    log.warn("Task ${getId()} encountered ${ex.message} - sleeping 10 seconds")
                    queue.suspend(10)
                }
                429, 509 -> {
                    log.warn("Task ${getId()} encountered ${ex.message} - sleeping 60 seconds")
                    queue.suspend(60)
                }
                else -> log.warn("Task ${getId()} encountered ${ex.message}")
            }
        } catch (ex: Exception) {
            log.error("Task ${getId()} encountered exception", ex)
            queue.suspend(1)
        }

        if (attempt < CommandLineOpts.commandLineOpts.tries) {
            queue.add(this)
        } else {
            reporter.error()
            log.error(String.format("Task %d did not complete - %s", id, this.toString()))
        }
    }

    override fun compareTo(o: Task): Int {
        return o.priority() - priority()
    }

    class TaskOptions(val queue: TaskQueue, val api: OneDriveProvider, val fileSystem: FileSystemProvider, val reporter: TaskReporter)

    protected fun isIgnored(remoteFile: OneDriveItem): Boolean {
        val ignored = isIgnored(remoteFile.name + if (remoteFile.isDirectory) File.separator else "")

        if (ignored) {
            log.debug(String.format("Skipping ignored remote file %s", remoteFile.fullName))
        }

        return ignored
    }

    companion object {

        private val log = LogManager.getLogger(Task::class.java.name)
        private val taskIdCounter = AtomicInteger(1)

        private fun isIgnored(name: String): Boolean {
            val ignoredSet = CommandLineOpts.commandLineOpts.getIgnored()
            return null != ignoredSet && ignoredSet.contains(name)
        }
    }

    private fun isSizeInvalid(filename: String, size: Long): Boolean {
        val maxSizeKb = CommandLineOpts.commandLineOpts.maxSizeKb
        if (0 < maxSizeKb && size > (maxSizeKb * 1024).toLong()) {
            log.debug(String.format("Skipping file %s - size is %dKB (bigger than maximum of %dKB)",
                    filename,
                    size / 1024L,
                    maxSizeKb))
            return true
        }

        return false
    }

    protected fun isSizeInvalid(remoteFile: OneDriveItem): Boolean {
        return isSizeInvalid(remoteFile.fullName, remoteFile.size)
    }

    protected fun isSizeInvalid(localFile: File): Boolean {
        return isSizeInvalid(localFile.path, localFile.length())
    }

    protected fun isIgnored(localFile: File): Boolean {
        val ignored = isIgnored(localFile.name + if (localFile.isDirectory) File.separator else "")

        if (ignored) {
            log.debug(String.format("Skipping ignored local file %s", localFile.path))
        }

        return ignored
    }
}
