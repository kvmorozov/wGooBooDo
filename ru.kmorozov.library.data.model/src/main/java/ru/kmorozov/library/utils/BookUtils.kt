package ru.kmorozov.library.utils

import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat
import ru.kmorozov.library.data.model.book.Category
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by km on 26.12.2016.
 */
object BookUtils {

    private val logger = Logger.getLogger(BookUtils::class.java.name)

    fun getFormat(fileName: String): BookFormat {
        for (format in BookFormat.values())
            if (fileName.endsWith(format.ext))
                return format

        logger.log(Level.INFO, "Unknown format for file $fileName")
        return BookFormat.UNKNOWN
    }

    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit.toLong()) return bytes.toString() + " B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes.toDouble() / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun mergeCategories(bookFrom: Book, bookTo: Book) {
        if (bookFrom === bookTo)
            return

        val inheritedFrom = bookFrom.storage!!.categories
        val inheritedTo = bookTo.storage!!.categories

        val ownFrom = if (bookFrom.categories == null) HashSet() else bookFrom.categories
        val ownTo = if (bookTo.categories == null) HashSet() else bookTo.categories

        val merged = HashSet<Category>()
        val inheritedMerged = HashSet<Category>()

        if (ownTo != null) {
            merged.addAll(ownTo)
        }
        if (ownFrom != null) {
            merged.addAll(ownFrom)
        }

        inheritedMerged.addAll(inheritedFrom!!)
        inheritedMerged.removeAll(inheritedTo!!)

        merged.addAll(inheritedMerged)

        bookTo.categories = merged
    }
}
