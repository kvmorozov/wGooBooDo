package ru.kmorozov.library.data.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by km on 19.12.2016.
 */
@SpringBootApplication
public class LibraryRestServer {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryRestServer.class, args);
    }
}
