package ru.kmorozov.onedrive.client

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class OneDriveUploadSession @Throws(IOException::class)
constructor(val parent: OneDriveItem, val file: File, val uploadUrl: String, ranges: Array<String>) {
    private val raf: RandomAccessFile
    private var ranges: Array<Range?>? = null
    var totalUploaded: Long = 0
        private set
    var lastUploaded: Long = 0
        private set
    var item: OneDriveItem? = null
        private set

    val chunk: ByteArray
        @Throws(IOException::class)
        get() {

            var bytes = ByteArray(CHUNK_SIZE)

            raf.seek(totalUploaded)
            val read = raf.read(bytes)

            if (CHUNK_SIZE > read) {
                bytes = Arrays.copyOf(bytes, read)
            }

            return bytes
        }

    val isComplete: Boolean
        get() = null != item

    init {
        this.raf = RandomAccessFile(file, "r")
        setRanges(ranges)
    }

    fun setRanges(stringRanges: Array<String>) {

        this.ranges = arrayOfNulls(stringRanges.size)
        for (i in stringRanges.indices) {
            val start = java.lang.Long.parseLong(stringRanges[i].substring(0, stringRanges[i].indexOf('-')))

            val s = stringRanges[i].substring(stringRanges[i].indexOf('-') + 1)

            var end = 0L
            if (!s.isEmpty()) {
                end = java.lang.Long.parseLong(s)
            }

            ranges!![i] = Range(start, end)
        }

        if (ranges!!.isNotEmpty()) {
            lastUploaded = ranges!![0]!!.start - totalUploaded
            totalUploaded = ranges!![0]!!.start
        }
    }

    fun setComplete(item: OneDriveItem) {
        this.item = item
        lastUploaded = file.length() - totalUploaded
        totalUploaded = file.length()
    }

    private class Range(var start: Long, var end: Long)

    companion object {

        // Upload in chunks of 5MB as per MS recommendation
        private const val CHUNK_SIZE = 5 * 1024 * 1024
    }
}
