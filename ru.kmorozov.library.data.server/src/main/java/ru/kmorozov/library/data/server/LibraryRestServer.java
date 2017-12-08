package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.converters.mongo.MongoConverterUtils;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.utils.DuplicatesProcessor;

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackageClasses = {MongoConfiguration.class, LibraryRestController.class, LoaderConfiguration.class,
        MongoConverterUtils.class, DuplicatesProcessor.class})
public class LibraryRestServer {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
