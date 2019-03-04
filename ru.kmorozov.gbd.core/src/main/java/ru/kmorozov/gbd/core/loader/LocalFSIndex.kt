package ru.kmorozov.gbd.core.loader

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.streams.toList

class LocalFSIndex(private val storage: LocalFSStorage, indexName: String, createIfNotExists: Boolean) : IIndex {

    private val indexFile: File

    private var loaded: Boolean = false

    private var booksMap: MutableMap<String, IBookInfo> = emptyMap<String, IBookInfo>().toMutableMap();

    override val books: List<IBookInfo>
        get() = getOrLoadBooks()

    override val bookIdsList: Set<String>
        get() = if (loaded) booksMap.keys else getOrLoadBooks().map { it.bookId }.toSet()

    init {
        indexFile = File(storage.storageDir.path + File.separator + indexName)
        if (!indexFile.exists() && createIfNotExists) {
            try {
                indexFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getOrLoadBooks(): List<IBookInfo> {
        if (!loaded) {
            if (indexFile.exists()) {
                lateinit var ctxObjArr: Array<BookInfo>
                try {
                    FileReader(indexFile).use { reader -> ctxObjArr = Mapper.gson.fromJson(reader, Array<BookInfo>::class.java) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                booksMap = ctxObjArr.toList().filter { it.bookId.isNotEmpty() }.associateBy { it.bookId }.toMutableMap()
            } else
                booksMap = getFromStorage().associateBy { it.bookId }.toMutableMap()

            loaded = true
        }

        return booksMap.values.toList()
    }

    override fun updateIndex(books: List<IBookInfo>) {
        try {
            FileWriter(indexFile).use { writer -> Mapper.gson.toJson(books, writer) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFromStorage(): List<IBookInfo> {
        var result = mutableListOf<LazyBookInfo>()
        return storage.bookIdsList.mapTo(result, { bookId -> LazyBookInfo(bookId, this) })
    }

    override fun updateBook(book: IBookInfo) {
        booksMap.put(book.bookId, book)
    }

    override fun updateContext() {
        if (!StringUtils.isEmpty(GBDOptions.bookId)) return

        if (ExecutionContext.initialized) {
            booksMap = ExecutionContext.INSTANCE.getContexts(false).stream()
                    .map(BookContext::bookInfo).toList().associate({ it.bookId to it }).toMutableMap()
            updateIndex(ArrayList<IBookInfo>(booksMap.values))
        } else {
            booksMap = getFromStorage().associate({ it.bookId to it }).toMutableMap()
            booksMap.toMutableMap().putAll(books.associate({ it.bookId to it }))
        }
    }

    override fun getBookInfo(bookId: String): IBookInfo {
        return booksMap.getOrDefault(bookId, BookInfo.EMPTY_BOOK)
    }
}
