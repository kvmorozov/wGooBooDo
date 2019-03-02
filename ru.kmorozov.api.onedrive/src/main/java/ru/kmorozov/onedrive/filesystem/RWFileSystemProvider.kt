package ru.kmorozov.onedrive.filesystem

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.Date

internal class RWFileSystemProvider : ROFileSystemProvider() {

    @Throws(IOException::class)
    private fun removeRecursive(path: Path) {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (null == exc) {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                } else {
                    // directory iteration failed; propagate exception
                    throw exc
                }
            }
        })
    }

    @Throws(IOException::class)
    override fun delete(file: File) {
        removeRecursive(file.toPath())
    }

    @Throws(IOException::class)
    override fun createFolder(file: File, name: String): File {
        val newFolder = File(file, name)

        if (!newFolder.mkdir()) {
            throw IOException("Unable to create local directory '$name' in '${file.name}'")
        }

        return newFolder
    }

    @Throws(IOException::class)
    override fun replaceFile(original: File, replacement: File) {
        replaceFileInternal(original, replacement, 0, 10)
    }

    @Throws(IOException::class)
    private fun replaceFileInternal(original: File, replacement: File, currentTry: Int, maxRetries: Int) {
        if (currentTry >= maxRetries)
            throw IOException("Unable to replace local file" + original.path)

        if (original.exists() && !original.delete()) {
            try {
                Thread.sleep(500L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            replaceFileInternal(original, replacement, currentTry + 1, maxRetries)
        }

        if (!replacement.renameTo(original)) {
            try {
                Thread.sleep(500L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            replaceFileInternal(original, replacement, currentTry + 1, maxRetries)
        }
    }

    @Throws(IOException::class)
    override fun setAttributes(downloadFile: File, created: Date, lastModified: Date) {
        val attributes = Files.getFileAttributeView(downloadFile.toPath(), BasicFileAttributeView::class.java)
        val createdFt = FileTime.fromMillis(created.time)
        val lastModifiedFt = FileTime.fromMillis(lastModified.time)
        attributes.setTimes(lastModifiedFt, lastModifiedFt, createdFt)
    }

    @Throws(IOException::class)
    override fun verifyCrc(file: File, crc: Long): Boolean {
        return getChecksum(file) == crc
    }
}
