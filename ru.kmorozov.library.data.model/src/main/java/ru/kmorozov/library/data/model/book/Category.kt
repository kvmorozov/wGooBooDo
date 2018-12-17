package ru.kmorozov.library.data.model.book

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.HashSet

/**
 * Created by km on 26.12.2016.
 */

@Document
class Category {

    @Id
    lateinit var id: String

    @Indexed(unique = true)
    lateinit var name: String

    @DBRef(lazy = true)
    internal var parents: MutableSet<Category>? = null

    @DBRef(lazy = true)
    internal var storages: MutableSet<Storage>? = null

    fun getParents(): Set<Category>? {
        return parents
    }

    fun setParents(parents: MutableSet<Category>) {
        this.parents = parents
    }

    fun getStorages(): Iterable<Storage>? {
        return storages
    }

    fun setStorages(storages: MutableSet<Storage>) {
        this.storages = storages
    }

    fun addStorage(storage: Storage) {
        if (null == storages)
            storages = HashSet()

        storages!!.add(storage)
    }

    fun addParents(items: Collection<Category>) {
        if (null == parents)
            parents = HashSet()

        parents!!.addAll(items)
    }

    fun addParent(parent: Category) {
        if (null == parents)
            parents = HashSet()

        parents!!.add(parent)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (null == o || javaClass != o.javaClass) return false

        val category = o as Category?

        return id == category!!.id

    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
