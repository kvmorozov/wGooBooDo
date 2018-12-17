package ru.kmorozov.library.data.loader.impl

import ru.kmorozov.library.data.model.book.BookInfo.BookFormat
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.model.book.Storage.StorageType
import ru.kmorozov.onedrive.client.OneDriveItem
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.utils.BookUtils

import java.io.File
import java.nio.file.Path
import java.util.Date

/**
 * Created by sbt-morozov-kv on 16.03.2017.
 */
class ServerItem {

    val isDirectory: Boolean
    val url: String
    val name: String
    var parent: ServerItem? = null
        private set
    lateinit var lastModifiedDateTime: Date
    val storageType: StorageType
    var filesCount: Long = 0
        private set
    val size: Long
    val originalItem: Any

    val isLoadableItem: Boolean
        get() {
            if (isDirectory)
                return true
            else {
                val format = BookUtils.getFormat(name)
                return BookFormat.UNKNOWN !== format && BookFormat.LNK !== format
            }
        }

    val isLink: Boolean
        get() = BookFormat.LNK === BookUtils.getFormat(name)

    val isLoadableOrLink: Boolean
        get() = isLoadableItem || isLink

    private constructor(oneDriveItem: OneDriveItem, lookupParent: Boolean) {
        this.isDirectory = oneDriveItem.isDirectory
        this.url = oneDriveItem.id!!
        this.name = oneDriveItem.name
        this.lastModifiedDateTime = oneDriveItem.lastModifiedDateTime
        this.storageType = StorageType.OneDrive
        this.originalItem = oneDriveItem
        this.size = oneDriveItem.size

        if (lookupParent && null != oneDriveItem.parent && null != oneDriveItem.parent!!.id)
            parent = ServerItem(oneDriveItem.parent!!, false)

        if (null != oneDriveItem.folder)
            filesCount = oneDriveItem.folder!!.childCount
    }

    internal constructor(oneDriveItem: OneDriveItem) : this(oneDriveItem, true) {}

    private constructor(path: Path, lookupParent: Boolean) {
        val file = path.toFile()

        this.isDirectory = file.isDirectory
        this.url = path.toString()
        this.name = file.name
        this.storageType = StorageType.LocalFileSystem
        this.originalItem = path
        this.size = file.length()

        if (lookupParent)
            parent = ServerItem(path.parent, false)
    }

    internal constructor(path: Path) : this(path, true) {}
}
