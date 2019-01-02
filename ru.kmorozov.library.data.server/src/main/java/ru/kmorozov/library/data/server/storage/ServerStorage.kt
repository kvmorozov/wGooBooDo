package ru.kmorozov.library.data.server.storage

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.gbd.core.config.IIndex
import ru.kmorozov.gbd.core.config.IStorage
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.library.LibraryFactory
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.onedrive.client.OneDriveProvider

import java.io.IOException
import java.nio.file.Path
import java.util.HashSet
import java.util.stream.Stream

class ServerStorage(private val api: OneDriveProvider, private val root: OneDriveItem?) : IStorage {

    private var children: Array<OneDriveItem>? = null

    override val isValidOrCreate: Boolean
        get() = root != null && root.isDirectory

    override val bookIdsList: Set<String>
        @Throws(IOException::class)
        get() {
            val bookIdsList = HashSet<String>()

            for (child in getChildren())
                if (child.isDirectory) {
                    val nameParts = child.name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (LibraryFactory.isValidId(nameParts[nameParts.size - 1]))
                        bookIdsList.add(nameParts[nameParts.size - 1])
                }

            return bookIdsList
        }

    override val items: Stream<IStoredItem>?
        @Throws(IOException::class)
        get() = throw IllegalStateException()

    @Throws(IOException::class)
    override fun getChildStorage(bookData: IBookData): IStorage {
        for (child in getChildren())
            if (child.name.contains(bookData.volumeId!!))
                return ServerStorage(api, child)

        val volumeId = bookData.volumeId
        val normalizedName = bookData.title!!
                .replace(":", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("/", ".")
        val childName = if (StringUtils.isEmpty(volumeId)) normalizedName else normalizedName + ' '.toString() + bookData.volumeId

        return ServerStorage(api, api.createFolder(root!!, childName))
    }

    @Throws(IOException::class)
    override fun size(): Int {
        return api.getChildren(root!!).size
    }

    @Throws(IOException::class)
    override fun isPageExists(page: IPage): Boolean {
        for (child in getChildren())
            if (!child.isDirectory)
                if (child.name.contains(page.order.toString() + "_" + page.pid + "."))
                    return true

        return false
    }

    @Throws(IOException::class)
    override fun getStoredItem(page: IPage, imgFormat: String): IStoredItem {
        return ServerStoredItem(this, page, imgFormat)
    }

    override fun refresh() {
        children = null
    }

    override fun getIndex(indexName: String, createIfNotExists: Boolean): IIndex {
        TODO("Not implemented yet")
    }

    override fun restoreState(bookInfo: IBookInfo) {
        throw IllegalStateException()
    }

    @Throws(IOException::class)
    private fun getChildren(): Array<OneDriveItem> {
        if (children == null)
            children = api.getChildren(root!!)

        return children as Array<OneDriveItem>
    }

    @Throws(IOException::class)
    internal fun deleteItem(item: OneDriveItem) {
        api.delete(item)
    }

    @Throws(IOException::class)
    internal fun saveItem(item: ServerStoredItem): OneDriveItem {
        val result = if (isPageExists(item.page)) api.replaceFile(root!!, item.asFile()) else api.uploadFile(root!!, item.asFile())

        refresh()

        return result
    }
}
