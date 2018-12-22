package ru.kmorozov.gbd.core.config

import java.io.File
import java.io.IOException
import java.io.OutputStream

interface IStoredItem {

    val outputStream: OutputStream?

    @Throws(IOException::class)
    fun exists(): Boolean

    @Throws(IOException::class)
    fun delete()

    @Throws(IOException::class)
    fun close()

    @Throws(IOException::class)
    fun write(bytes: ByteArray, read: Int)

    fun asFile(): File
}