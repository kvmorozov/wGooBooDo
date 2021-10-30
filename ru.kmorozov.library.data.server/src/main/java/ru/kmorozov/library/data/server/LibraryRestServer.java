package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.kmorozov.library.data.server.controllers.LibraryRestController;
import ru.kmorozov.library.data.server.controllers.StorageController;
import ru.kmorozov.library.data.server.scheduler.ProxyUpdater;

@EnableScheduling
@SpringBootApplication(scanBasePackageClasses = {StorageController.class,
        LibraryRestController.class, ProxyUpdater.class})
public class LibraryRestServer {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
