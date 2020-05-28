package ru.kmorozov.library.data.loader.utils

import com.google.common.base.Strings
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.model.dto.BookDTO
import ru.kmorozov.onedrive.client.OneDriveUrl
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by sbt-morozov-kv on 14.04.2017.
 */
object BookUtils {

    fun createBookDIO(book: Book): BookDTO {
        val dto = BookDTO(book, bookLoaded(book))
        when (book.storage!!.storageType) {
            Storage.StorageType.LocalFileSystem -> dto.path = book.bookInfo.path!!
            Storage.StorageType.OneDrive -> dto.path = OneDriveUrl.content(book.bookInfo.path!!).toString()
        }

        return dto
    }

    fun bookLoaded(book: Book): Boolean {
        val bookPath = book.storage!!.localPath + File.separator + book.bookInfo.fileName
        return if (!Strings.isNullOrEmpty(bookPath)) {
            Files.exists(Paths.get(bookPath))
        } else false

    }
}
