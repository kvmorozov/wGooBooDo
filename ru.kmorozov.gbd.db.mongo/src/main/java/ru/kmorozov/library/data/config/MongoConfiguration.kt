package ru.kmorozov.library.data.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Configuration
open class MongoConfiguration : AbstractMongoClientConfiguration() {

    @Value("\${mongo.uri}")
    private lateinit var mongoUri: String

    override fun getDatabaseName(): String {
        return BOOKS_MONGO_DB_NAME
    }

    override fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    override fun getMappingBasePackages(): Collection<String> {
        return setOf("ru.kmorozov.library.data.model")
    }

    companion object {

        const val BOOKS_MONGO_DB_NAME = "BOOKS"
    }
}
