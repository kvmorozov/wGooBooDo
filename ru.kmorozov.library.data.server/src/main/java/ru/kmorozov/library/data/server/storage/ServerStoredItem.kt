package ru.kmorozov.library.data.server.storage

import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.loader.LocalFSStoredItem
import ru.kmorozov.gbd.core.loader.TempLocalItem
import ru.kmorozov.gbd.core.loader.TempLocalStorage
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.onedrive.client.OneDriveItem

import java.io.File
import java.io.IOException
import java.io.OutputStream

class ServerStoredItem @Throws(IOException::class)
internal constructor(private val storage: ServerStorage, val page: IPage, imgFormat: String) : IStoredItem {
    override val createdNew: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private val localItem: LocalFSStoredItem
    private var remoteItem: OneDriveItem? = null

    override val outputStream: OutputStream?
        @Throws(IOException::class)
        get() = localItem.outputStream

    init {

        localItem = TempLocalItem(TempLocalStorage.DEFAULT_TEMP_STORAGE, page, imgFormat)
    }

    @Throws(IOException::class)
    override fun exists(): Boolean {
        return storage.isPageExists(page)
    }

    @Throws(IOException::class)
    override fun delete() {
        if (remoteItem != null)
            storage.deleteItem(remoteItem!!)

        if (localItem.exists())
            localItem.delete()
    }

    @Throws(IOException::class)
    override fun close() {
        remoteItem = storage.saveItem(this)
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray, len: Int) {
        localItem.write(bytes, len)
    }

    override fun asFile(): File {
        return localItem.asFile()
    }
}
