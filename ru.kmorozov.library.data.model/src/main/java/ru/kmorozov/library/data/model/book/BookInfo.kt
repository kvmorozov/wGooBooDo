package ru.kmorozov.library.data.model.book

import org.springframework.data.mongodb.core.index.Indexed

import java.util.Date

/**
 * Created by km on 26.12.2016.
 */

class BookInfo {

    var bookId: String? = null

    lateinit var format: BookFormat

    var bookType: BookType? = null

    var fileName: String? = null

    var lastModifiedDateTime: Date? = null

    @Indexed(unique = true)
    var path: String? = null

    var size: Long = 0

    private var customFields: MutableMap<String, String>? = null

    internal val isLink: Boolean
        get() = BookFormat.LNK == format

    enum class BookFormat(val ext: String) {
        PDF("pdf"),
        DJVU("djvu"),
        DOC("doc"),
        DOCX("docx"),
        LNK("lnk"),
        UNKNOWN("")
    }

    enum class BookType {
        ARTICLE,
        GOOGLE_BOOK
    }

    fun getCustomFields(): Map<String, String>? {
        return this.customFields
    }

    fun setCustomFields(customFields: MutableMap<String, String>) {
        if (this.customFields == null || this.customFields!!.isEmpty())
            this.customFields = customFields
        else
            this.customFields!!.putAll(customFields)
    }
}
