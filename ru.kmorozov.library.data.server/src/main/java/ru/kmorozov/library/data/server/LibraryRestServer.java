package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.config.RxMongoConfiguration;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.processors.DuplicatesProcessor;
import ru.kmorozov.library.data.loader.processors.JstorProcessor;

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackageClasses = {MongoConfiguration.class, LibraryRestController.class, LoaderConfiguration.class,
        DuplicatesProcessor.class, JstorProcessor.class, RxMongoConfiguration.class})
public class LibraryRestServer {

    public static void main(final String[] args) {
        GBDOptions.init(new ServerGBDOptions());

        SpringApplication.run(LibraryRestServer.class, args);
    }
}
