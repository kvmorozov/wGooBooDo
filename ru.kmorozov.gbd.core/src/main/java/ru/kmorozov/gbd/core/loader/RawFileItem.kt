package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path

open class RawFileItem : IStoredItem {
    protected val outputFile: File

    public override val createdNew: Boolean

    constructor(outputFile: File) {
        this.outputFile = outputFile
        this.createdNew = !outputFile.exists()
    }

    constructor(path: Path) : this(path.toFile()) {}

    override lateinit var outputStream: OutputStream

    var totalLen = 0

    @Throws(IOException::class)
    override fun exists(): Boolean {
        return outputFile.exists()
    }

    @Throws(IOException::class)
    override fun delete() {
        try {
            if (totalLen > 0)
                outputStream.close()

            outputFile.delete()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        outputStream.close()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, len: Int) {
        try {
            if (totalLen == 0)
                outputStream = FileOutputStream(outputFile)

            outputStream.write(bytes, 0, len)
            totalLen += len
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun asFile(): File {
        return outputFile
    }
}
