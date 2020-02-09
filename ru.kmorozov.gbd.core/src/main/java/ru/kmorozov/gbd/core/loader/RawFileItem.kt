package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.utils.Images

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

    override val page: IPage
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override lateinit var outputStream: OutputStream

    var totalLen = 0

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
                outputStream.close()
                totalLen = 0;
            }

            outputFile.delete()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (totalLen > 0)
            outputStream.close()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, len: Int) {
        try {
            if (totalLen == 0)
                init()

            writeInternal(bytes, len)
            totalLen += len
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    open fun init() {
        outputStream = FileOutputStream(outputFile)
    }

    fun writeInternal(bytes: ByteArray, len: Int) {
        outputStream.write(bytes, 0, len)
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
