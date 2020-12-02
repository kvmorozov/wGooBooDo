package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.utils.Images
import java.io.*
import java.nio.file.Path

open class RawFileItem : IStoredItem {
    protected val outputFile: File

    override val createdNew: Boolean

    constructor(outputFile: File) {
        this.outputFile = outputFile
        this.createdNew = !outputFile.exists()
    }

    constructor(path: Path) : this(path.toFile()) {}

    override lateinit var outputStream: OutputStream

    var totalLen = 0L

    override val pageNum: Int
        get() = Integer.parseInt(outputFile.toPath().fileName.toString().split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])

    @Throws(IOException::class)
    override fun exists(): Boolean {
        return outputFile.exists()
    }

    @Throws(IOException::class)
    override fun delete() {
        try {
            if (totalLen > 0) {
                totalLen = 0;
            }

            outputFile.delete()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun close() {
    }

    @Throws(IOException::class)
    override fun write(inStream: InputStream) {
        FileOutputStream(outputFile).use { totalLen += inStream.transferTo(it) }
    }

    open fun init() {

    }

    override fun flush() {

    }

    override fun asFile(): File {
        return outputFile
    }

    override fun isImage(): Boolean {
        return Images.isImageFile(outputFile.toPath())
    }

    override fun validate(): Boolean {
        return outputFile.exists()
    }
}
