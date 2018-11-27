package ru.kmorozov.library.data.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"ru.kmorozov.library.data.client.netty", "ru.kmorozov.library.data.client"})
public class LibraryClient {

    public static void main(String[] args) {
        SpringApplication.run(LibraryClient.class, args);
    }

    @Bean
    static RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
