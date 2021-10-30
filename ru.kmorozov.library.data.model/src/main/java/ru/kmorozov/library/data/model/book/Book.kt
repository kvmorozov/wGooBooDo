package ru.kmorozov.library.data.model.book

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

import java.util.HashMap

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Document
class Book {

    @Id
    lateinit var bookId: String
    @TextIndexed
    var title: String? = null
    @TextIndexed
    lateinit var author: String

    lateinit var bookInfo: BookInfo

    var linkInfo: LinkInfo? = null

    @DBRef
    var storage: Storage? = null

    @DBRef(lazy = true)
    var categories: Set<Category>? = null

    internal var bookIds: MutableMap<IdType, String>? = null

    val bookKey: String?
        get() = bookInfo.fileName

    val isLink: Boolean
        get() = this.bookInfo.isLink

    val isBrokenLink: Boolean
        get() = this.bookInfo.isLink && (null == this.linkInfo || this.linkInfo!!.isBroken)

    constructor()

    fun getBookIds(): Map<IdType, String>? {
        return this.bookIds
    }

    fun setBookIds(bookIds: MutableMap<IdType, String>) {
        this.bookIds = bookIds
    }

    constructor(title: String, author: String) {
        this.title = title
        this.author = author
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other || javaClass != other.javaClass) return false
        if (null == title) return false

        val book = other as Book?

        return if (title != book!!.title) false else author == book.author
    }

    override fun hashCode(): Int {
        if (null == title)
            return super.hashCode()

        var result = title!!.hashCode()
        result = 31 * result + author.hashCode()
        return result
    }

    fun addBookId(idType: IdType, bookId: String) {
        bookIds = if (bookIds == null) HashMap(1) else bookIds
        bookIds!![idType] = bookId
    }
}
