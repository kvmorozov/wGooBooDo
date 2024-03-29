package ru.kmorozov.library.data.model.dto

import kotlin.streams.toList
import org.springframework.hateoas.RepresentationModel

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
class ItemDTO : RepresentationModel<ItemDTO> {

    lateinit var itemId: String
        private set
    lateinit var itemType: ItemType
        private set
    lateinit var itemSubType: Any
        private set
    lateinit var displayName: String
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

    constructor()

    constructor(storageDTO: StorageDTO) {
        this.itemId = storageDTO.id
        this.itemType = ItemType.storage
        this.itemSubType = storageDTO.storageType
        this.displayName = storageDTO.displayName
        this.filesCount = storageDTO.filesCount

        refreshStatus = if (REFRESH_INTERVAL.toLong() > System.currentTimeMillis() - storageDTO.lastChecked) RefreshStatus.updated else RefreshStatus.dirty

        if (null != storageDTO.categories)
            this.categories = storageDTO.categories!!.stream().map { CategoryDTO(it) }.toList()
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
