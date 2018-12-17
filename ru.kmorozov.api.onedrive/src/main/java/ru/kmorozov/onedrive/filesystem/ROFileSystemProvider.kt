package ru.kmorozov.onedrive.filesystem

import ru.kmorozov.onedrive.CommandLineOpts
import ru.kmorozov.onedrive.filesystem.FileSystemProvider.FileMatch

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

internal open class ROFileSystemProvider : FileSystemProvider {

    @Throws(IOException::class)
    override fun delete(file: File) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun createFolder(file: File, name: String): File {

        return object : File(file, name) {
            override fun isDirectory(): Boolean {
                return true
            }
        }
    }

    override fun createFile(file: File, name: String): File {
        return File(file, name)
    }

    @Throws(IOException::class)
    override fun replaceFile(original: File, replacement: File) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun setAttributes(downloadFile: File, created: Date, lastModified: Date) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun verifyCrc(file: File, crc: Long): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun verifyMatch(file: File, crc: Long, fileSize: Long, created: Date, lastModified: Date): FileMatch {
        var created = created
        var lastModified = lastModified

        // Round to nearest second
        created = Date(created.time / 1000L * 1000L)
        lastModified = Date(lastModified.time / 1000L * 1000L)

        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

        // Timestamp rounded to the nearest second
        val localCreatedDate = Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L)
        val localModifiedDate = Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L)

        val sizeMatches = fileSize == file.length()
        val createdMatches = created == localCreatedDate
        val modifiedMatches = lastModified == localModifiedDate

        if (!CommandLineOpts.commandLineOpts.useHash() && sizeMatches && createdMatches && modifiedMatches) {
            // Close enough!
            return FileMatch.YES
        }

        val localCrc = getChecksum(file)
        val crcMatches = crc == localCrc

        // If the crc matches but the timestamps do not we won't upload the content again
        return if (crcMatches && !(modifiedMatches && createdMatches)) {
            FileMatch.CRC
        } else if (crcMatches) {
            FileMatch.YES
        } else {
            FileMatch.NO
        }
    }

    @Throws(IOException::class)
    override fun verifyMatch(file: File, created: Date, lastModified: Date): FileMatch {
        var created = created
        var lastModified = lastModified

        // Round to nearest second
        created = Date(created.time / 1000L * 1000L)
        lastModified = Date(lastModified.time / 1000L * 1000L)

        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

        // Timestamp rounded to the nearest second
        val localCreatedDate = Date(attr.creationTime().to(TimeUnit.SECONDS) * 1000L)
        val localModifiedDate = Date(attr.lastModifiedTime().to(TimeUnit.SECONDS) * 1000L)

        val createdMatches = created == localCreatedDate
        val modifiedMatches = lastModified == localModifiedDate

        return if (createdMatches && modifiedMatches) {
            FileMatch.YES
        } else {
            FileMatch.NO
        }
    }

    @Throws(IOException::class)
    override fun getChecksum(file: File): Long {
        // Compute CRC32 checksum
        CheckedInputStream(FileInputStream(file), CRC32()).use { cis ->
            val buf = ByteArray(1024)

            while (0 <= cis.read(buf)) {
            }

            return cis.checksum.value
        }
    }
}
