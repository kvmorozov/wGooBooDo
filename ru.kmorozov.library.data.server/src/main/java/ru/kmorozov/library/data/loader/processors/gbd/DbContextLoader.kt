package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.library.data.repository.GoogleBooksRepository

import java.util.HashMap

@Component
class DbContextLoader : IContextLoader {

    private val booksMap = HashMap<String, BookInfo>()

    @Autowired
    @Lazy
    private val googleBooksRepository: GoogleBooksRepository? = null

    override val bookIdsList: Set<String>
        get() = booksMap.keys

    override val contextSize: Int
        get() = booksMap.size

    override val isValid: Boolean
        get() = false

    override fun updateIndex() {

    }

    override fun updateContext() {

    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        val existBookInfo = googleBooksRepository!!.findByBookId(bookInfo.bookId)
        if (existBookInfo != null)
            googleBooksRepository.delete(existBookInfo)

        googleBooksRepository.save(bookInfo)
    }

    override fun getBookInfo(bookId: String): BookInfo {
        var info: BookInfo? = booksMap[bookId]
        if (info == null) {
            info = googleBooksRepository!!.findByBookId(bookId)

            for (page in info!!.pages.pages)
                (page as AbstractPage).isFileExists = true

            booksMap[bookId] = info!!
        }
        return info
    }

    override fun refreshContext() {

    }

}
