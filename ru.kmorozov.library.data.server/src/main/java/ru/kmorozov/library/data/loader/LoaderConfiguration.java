package ru.kmorozov.library.data.loader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.kmorozov.library.data.config.MongoConfiguration;

/**
 * Created by km on 26.12.2016.
 */

@Configuration
@ComponentScan(basePackageClasses = {MongoConfiguration.class}, basePackages = {"ru.kmorozov.library.data.loader"})
public class LoaderConfiguration {

    private static final String LOCAL_DIR = "C:\\Users\\sbt-morozov-kv\\Desktop\\Документы\\Прочая документация";

    @Bean
    public String localBasePath() {
        return LOCAL_DIR;
    }

    @Bean
    public String oneDriveKeyFileName() {
        return "onedrive.key";
    }
}
