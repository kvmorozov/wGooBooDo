package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path

open class RawFileItem(protected val outputFile: File) : IStoredItem {
    constructor(path: Path) : this(path.toFile()) {}

    override val outputStream: OutputStream = FileOutputStream(outputFile)

    var totalLen = 0

    @Throws(IOException::class)
    override fun exists(): Boolean {
        return outputFile.exists()
    }

    @Throws(IOException::class)
    override fun delete() {
        outputFile.delete()
    }

    @Throws(IOException::class)
    override fun close() {
        outputStream.close()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, len: Int) {
        totalLen += len
        outputStream.write(bytes, 0, len)
    }

    override fun asFile(): File {
        return outputFile
    }
}
