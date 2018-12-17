package ru.kmorozov.library.data.model.dto

import ru.kmorozov.library.data.model.book.Category
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.model.book.Storage.StorageType

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
class StorageDTO {

    lateinit var id: String
    lateinit var storageType: StorageType
    lateinit var url: String
    lateinit var displayName: String
    var parentId: String? = null
    var filesCount: Long = 0
    var lastChecked: Long = 0
    public var categories: Set<Category>? = null
        private set

    constructor() {}

    @JvmOverloads
    constructor(storage: Storage, withCategories: Boolean = false) {
        this.id = storage.id
        this.storageType = storage.storageType
        this.url = storage.url
        this.displayName = storage.name
        this.parentId = if (null == storage.parent) null else storage.parent!!.id
        this.filesCount = if (null == storage.storageInfo) 0L else storage.storageInfo!!.filesCount
        this.lastChecked = if (null == storage.storageInfo) 0L else storage.storageInfo!!.lastChecked

        if (withCategories)
            this.categories = storage.categories
    }

    fun getCategories(): Collection<Category>? {
        return categories
    }
}
