package ru.kmorozov.gbd.core.config

import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import java.io.File
import java.io.IOException
import java.io.OutputStream

interface IStoredItem {

    val page: IPage

    val outputStream: OutputStream?

    @Throws(IOException::class)
    fun exists(): Boolean

    @Throws(IOException::class)
    fun delete()

    @Throws(IOException::class)
    fun close()

    @Throws(IOException::class)
    fun write(bytes: ByteArray, len: Int)

    fun asFile(): File

    val createdNew: Boolean

    val pageNum: Int
        get() = -1
}
