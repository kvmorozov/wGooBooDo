package ru.kmorozov.library.data.model

import ru.kmorozov.library.data.model.dto.*
import ru.kmorozov.library.data.model.dto.ItemDTO.ItemType

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
interface IDataRestServer {

    fun login(login: String): UserDTO

    fun getStoragesByParentId(storageId: String): List<StorageDTO>

    fun getBooksByStorageId(storageId: String): List<BookDTO>

    fun getItemsByStorageId(storageId: String): List<ItemDTO>

    fun getItem(itemId: String, itemType: ItemType, refresh: Boolean): ItemDTO?

    fun updateLibrary(state: String)

    fun downloadBook(bookId: String): BookDTO

    fun findDuplicates(): List<DuplicatedBookDTO>

    fun synchronizeDb()
}
