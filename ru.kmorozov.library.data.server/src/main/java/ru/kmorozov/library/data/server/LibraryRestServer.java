package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.server.controllers.LibraryRestController;
import ru.kmorozov.library.data.server.controllers.StorageController;

@SpringBootApplication(scanBasePackageClasses = {MongoConfiguration.class, StorageController.class, LibraryRestController.class})
public class LibraryRestServer {

    public static void main(String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
