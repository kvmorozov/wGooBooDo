package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.db.utils.Mapper

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class LocalFSIndex(private val storage: LocalFSStorage, indexName: String, createIfNotExists: Boolean) : IIndex {

    private val indexFile: File

    override val books: Array<IBookInfo>
        get() {
            if (indexFile.exists()) {
                lateinit var ctxObjArr: Array<BookInfo>
                try {
                    FileReader(indexFile).use { reader -> ctxObjArr = Mapper.gson.fromJson(reader, Array<BookInfo>::class.java) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return (ctxObjArr as Array<IBookInfo>).filter { it.bookId.isNotEmpty() }.toTypedArray()
            }
            else
                return arrayOf(BookInfo.EMPTY_BOOK)
        }

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

    override fun updateIndex(books: List<IBookInfo>) {
        try {
            FileWriter(indexFile).use { writer -> Mapper.gson.toJson(books, writer) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
