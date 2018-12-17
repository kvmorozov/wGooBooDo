package ru.kmorozov.onedrive.filesystem

import java.io.File
import java.io.IOException
import java.util.Date

interface FileSystemProvider {

    @Throws(IOException::class)
    fun delete(file: File)

    @Throws(IOException::class)
    fun createFolder(file: File, name: String): File

    fun createFile(file: File, name: String): File

    @Throws(IOException::class)
    fun replaceFile(original: File, replacement: File)

    @Throws(IOException::class)
    fun setAttributes(downloadFile: File, created: Date, lastModified: Date)

    @Throws(IOException::class)
    fun verifyCrc(file: File, crc: Long): Boolean

    @Throws(IOException::class)
    fun verifyMatch(file: File, crc: Long, fileSize: Long, created: Date, lastModified: Date): FileMatch

    @Throws(IOException::class)
    fun verifyMatch(file: File, created: Date, lastModified: Date): FileMatch

    /**
     * Get the CRC32 Checksum for a file
     *
     * @param file The file to check
     * @return The CRC32 checksum of the file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getChecksum(file: File): Long

    enum class FileMatch {
        YES,
        CRC,
        NO
    }

    object FACTORY {

        fun readOnlyProvider(): FileSystemProvider {
            return ROFileSystemProvider()
        }

        fun readWriteProvider(): FileSystemProvider {
            return RWFileSystemProvider()
        }
    }

}
