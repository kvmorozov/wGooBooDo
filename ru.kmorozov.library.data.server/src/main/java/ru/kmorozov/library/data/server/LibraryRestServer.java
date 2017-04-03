package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.loader.LoaderConfiguration;

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {MongoConfiguration.class, LibraryRestController.class, LoaderConfiguration.class})
public class LibraryRestServer {

    public static void main(String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
