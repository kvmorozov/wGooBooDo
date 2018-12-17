package ru.kmorozov.library.data.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import ru.kmorozov.library.data.config.MongoConfiguration
import ru.kmorozov.library.data.server.controllers.LibraryRestController
import ru.kmorozov.library.data.server.controllers.StorageController

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication(scanBasePackageClasses = arrayOf(MongoConfiguration::class, StorageController::class, LibraryRestController::class))
object LibraryRestServer {

    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(LibraryRestServer::class.java, *args)
    }
}
