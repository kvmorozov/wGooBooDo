package ru.kmorozov.library.data.loader.processors.gbd

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider
import java.io.IOException

@Component
open class OneDriveContextLoader : IContextLoader {
    override val empty: Boolean
        get() = false

    @Autowired
    @Lazy
    private lateinit var api: OneDriveProvider

    @Autowired
    @Lazy
    private lateinit var dbContextLoader: DbContextLoader

    private var booksMap: MutableMap<String, BookInfo> = HashMap()
    private var itemsMap: MutableMap<String, OneDriveItem> = HashMap()

    override val bookIdsList: Set<String>
        get() = booksMap.keys

    override val contextSize: Int
        get() = booksMap.size

    override val isValid: Boolean
        get() {
            try {
                return api.root != null
            } catch (e: IOException) {
                logger.error("Invalid OneDrive connection", e)
                return false
            }

        }

    internal fun initContext(root: OneDriveItem) {
        try {
            for (item in api.getChildren(root))
                if (item.isDirectory) {
                    val nameTokens = item.name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val bookId = nameTokens[nameTokens.size - 1]
                    booksMap[bookId] = BookInfo.EMPTY_BOOK
                    itemsMap[bookId] = item
                }
        } catch (e: IOException) {
            logger.error("Cannot init OneDriveContextLoader", e)
        }

    }

    override fun updateContext() {

    }

    override fun updateBookInfo(bookInfo: BookInfo) {
        dbContextLoader.updateBookInfo(bookInfo)
    }

    override fun getBookInfo(bookId: String): BookInfo {
        return booksMap.computeIfAbsent(bookId, { GoogleBookInfoExtractor(bookId, dbContextLoader).bookInfo })
    }

    fun getBookDir(bookId: String): OneDriveItem {
        return itemsMap[bookId]!!
    }

    companion object {

        protected val logger = Logger.getLogger(GBDOptions.debugEnabled, OneDriveContextLoader::class.java)
    }
}
