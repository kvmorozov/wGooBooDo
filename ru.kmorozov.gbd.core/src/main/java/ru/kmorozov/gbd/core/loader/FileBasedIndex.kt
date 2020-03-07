package ru.kmorozov.gbd.core.loader

import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class FileBasedIndex : LocalFSIndex {

    private val indexFile: File

    constructor(storage: LocalFSStorage, indexFile: File) : super(storage) {
        this.indexFile = indexFile
    }

    override fun updateIndex(books: List<IBookInfo>) {
        try {
            FileWriter(indexFile).use { writer -> Mapper.gson.toJson(books, writer) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    protected override fun getOrLoadBooks(): List<IBookInfo> {
        if (!loaded) {
            lateinit var ctxObjArr: Array<BookInfo>
            try {
                FileReader(indexFile).use { reader -> ctxObjArr = Mapper.gson.fromJson(reader, Array<BookInfo>::class.java) }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            booksMap = ctxObjArr.toList().filter { it.bookId.isNotEmpty() }.associateBy { it.bookId }.toMutableMap()
            loaded = true
        }

        return booksMap.values.toList()
    }

}