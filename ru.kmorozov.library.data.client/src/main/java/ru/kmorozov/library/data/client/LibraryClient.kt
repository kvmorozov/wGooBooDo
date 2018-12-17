package ru.kmorozov.library.data.client

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestTemplate

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = arrayOf("ru.kmorozov.library.data.client.netty", "ru.kmorozov.library.data.client"))
object LibraryClient {

    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(LibraryClient::class.java, *args)
    }

    @Bean
    internal fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
