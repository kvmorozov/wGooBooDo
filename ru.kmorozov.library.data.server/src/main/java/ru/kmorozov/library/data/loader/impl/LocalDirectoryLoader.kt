package ru.kmorozov.library.data.loader.impl

import org.springframework.stereotype.Component
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.Storage

import java.io.IOException

/**
 * Created by km on 26.12.2016.
 */

@Component
class LocalDirectoryLoader : BaseLoader() {

    override fun refresh(storage: Storage): Storage {
        return storage
    }

    @Throws(IOException::class)
    override fun load() {

    }

    override fun resolveLink(lnkBook: Book) {

    }

    override fun downloadBook(book: Book) {

    }

    fun processLinks() {}

    companion object {

        private val logger = Logger.getLogger(LocalDirectoryLoader::class.java)
    }
}