package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path

open class RawFileItem(protected val outputFile: File) : IStoredItem {
    constructor(path: Path) : this(path.toFile()) {}

    override var outputStream: OutputStream? = null
        @Throws(IOException::class)
        get() = FileOutputStream(outputFile)

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
        if (outputStream != null)
            outputStream!!.close()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, read: Int) {
        outputStream?.write(bytes, 0, read)
    }

    override fun asFile(): File {
        return outputFile
    }
}
