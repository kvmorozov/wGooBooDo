package ru.kmorozov.library.data.model.book

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.util.CollectionUtils
import java.util.*

/**
 * Created by km on 26.12.2016.
 */

@Document
class Storage {

    @Id
    lateinit var id: String

    lateinit var storageType: StorageType
    lateinit var url: String
    lateinit var name: String
    lateinit var lastModifiedDateTime: Date

    @DBRef(lazy = true)
    var parent: Storage? = null

    @DBRef(lazy = true)
    internal var categories: MutableSet<Category>? = null

    var storageInfo: StorageInfo? = null

    lateinit var localPath: String

    val mainCategory: Category?
        get() = if (CollectionUtils.isEmpty(categories)) null else categories!!.toTypedArray()[0]

    enum class StorageType {
        LocalFileSystem,
        OneDrive
    }

    fun getCategories(): Set<Category>? {
        return categories
    }

    fun setCategories(categories: MutableSet<Category>) {
        this.categories = categories
    }

    fun addCategory(category: Category) {
        if (null == categories)
            categories = HashSet()

        categories!!.add(category)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (null == o || javaClass != o.javaClass) return false

        val storage = o as Storage?

        return id == storage!!.id

    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
