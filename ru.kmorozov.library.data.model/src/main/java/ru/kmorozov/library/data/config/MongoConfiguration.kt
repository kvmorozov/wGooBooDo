package ru.kmorozov.library.data.config

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import java.util.Collections

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Configuration
open class MongoConfiguration : AbstractMongoConfiguration() {

    @Value("\${mongo.uri}")
    private val mongoUri: String? = null

    override fun getDatabaseName(): String {
        return BOOKS_MONGO_DB_NAME
    }

    override fun mongoClient(): MongoClient {
        return MongoClient(MongoClientURI(mongoUri!!))
    }

    override fun getMappingBasePackages(): Collection<String> {
        return setOf("ru.kmorozov.library.data.model")
    }

    companion object {

        const val BOOKS_MONGO_DB_NAME = "BOOKS"
    }
}
