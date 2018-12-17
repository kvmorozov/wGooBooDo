package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider

import java.io.IOException
import java.util.HashMap

@Component
class OneDriveContextLoader : IContextLoader {

    @Autowired
    @Lazy
    private val api: OneDriveProvider? = null

    @Autowired
    @Lazy
    private val dbContextLoader: DbContextLoader? = null

    private var booksMap: MutableMap<String, BookInfo>? = null
    private var itemsMap: MutableMap<String, OneDriveItem>? = null

    override val bookIdsList: Set<String>
        get() = booksMap!!.keys

    override val contextSize: Int
        get() = booksMap!!.size

    override val isValid: Boolean
        get() {
            if (api == null)
                return false

            try {
                return api.root != null
            } catch (e: IOException) {
                logger.error("Invalid OneDrive connection", e)
                return false
            }

        }

    internal fun initContext(root: OneDriveItem) {
        booksMap = HashMap()
        itemsMap = HashMap()

        try {
            for (item in api!!.getChildren(root))
                if (item.isDirectory) {
                    val nameTokens = item.name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val bookId = nameTokens[nameTokens.size - 1]
                    booksMap!![bookId] = BookInfo.EMPTY_BOOK
                    itemsMap!![bookId] = item
                }
        } catch (e: IOException) {
            logger.error("Cannot init OneDriveContextLoader", e)
        }

    }

    override fun updateIndex() {

    }

    override fun updateContext() {

    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        dbContextLoader!!.updateBookInfo(bookInfo)
    }

    override fun getBookInfo(bookId: String): BookInfo {
        var bookInfo: BookInfo? = booksMap!![bookId]
        if (bookInfo == null)
            booksMap!![bookId] = GoogleBookInfoExtractor(bookId, dbContextLoader!!).bookInfo

        return bookInfo!!
    }

    fun getBookDir(bookId: String): OneDriveItem {
        return itemsMap!![bookId]!!
    }

    override fun refreshContext() {

    }

    companion object {

        protected val logger = Logger.getLogger(OneDriveContextLoader::class.java)
    }
}
