package ru.kmorozov.library.data.model.dto

import org.springframework.hateoas.ResourceSupport
import kotlin.streams.toList

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
class ItemDTO : ResourceSupport {

    var itemId: String? = null
        private set
    var itemType: ItemType? = null
        private set
    var itemSubType: Any? = null
        private set
    var displayName: String? = null
        private set
    var filesCount: Long = 0
    var categories: List<CategoryDTO>? = null
        private set
    var refreshStatus: RefreshStatus? = null
        private set

    enum class ItemType {
        storage,
        category,
        book
    }

    enum class RefreshStatus {
        dirty,
        updated
    }

    constructor() {}

    constructor(storageDTO: StorageDTO) {
        this.itemId = storageDTO.id
        this.itemType = ItemType.storage
        this.itemSubType = storageDTO.storageType
        this.displayName = storageDTO.displayName
        this.filesCount = storageDTO.filesCount

        refreshStatus = if (ItemDTO.REFRESH_INTERVAL.toLong() > System.currentTimeMillis() - storageDTO.lastChecked) RefreshStatus.updated else RefreshStatus.dirty

        if (null != storageDTO.categories)
            this.categories = storageDTO.categories!!.stream().map<CategoryDTO> { CategoryDTO(it) }.toList()
    }

    constructor(bookDTO: BookDTO) {
        this.itemId = bookDTO.id
        this.itemType = ItemType.book
        this.itemSubType = bookDTO.format
        this.displayName = bookDTO.title
    }

    fun setUpdated() {
        this.refreshStatus = RefreshStatus.updated
    }

    companion object {

        const val REFRESH_INTERVAL = 10 * 1000 * 60
    }
}
