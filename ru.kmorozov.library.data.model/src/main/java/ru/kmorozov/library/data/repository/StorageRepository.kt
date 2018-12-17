package ru.kmorozov.library.data.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.kmorozov.library.data.model.book.Storage

/**
 * Created by km on 26.12.2016.
 */
interface StorageRepository : MongoRepository<Storage, String> {

    fun findByUrl(url: String): Storage?

    fun findAllByParent(parent: Storage): MutableList<Storage>

    fun findAllByName(name: String): List<Storage>
}
