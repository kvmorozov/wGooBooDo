package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {ru.kmorozov.library.data.config.MongoConfiguration.class, LibraryRestController.class})
public class LibraryRestServer {

    public static void main(String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
