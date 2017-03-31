package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import com.wouterbreukink.onedrive.client.exceptions.CodeExpiredException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.kmorozov.library.data.config.MongoConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by km on 26.12.2016.
 */

@Configuration
@ComponentScan(basePackageClasses = {MongoConfiguration.class}, basePackages = {"ru.kmorozov.library.data.loader"})
public class LoaderConfiguration {

    private static final Logger logger = Logger.getLogger(LoaderConfiguration.class);
    private static final String LOCAL_DIR = "C:\\Users\\sbt-morozov-kv\\Desktop\\Документы\\Прочая документация";

    @Bean
    public String localBasePath() {
        return LOCAL_DIR;
    }

    @Bean
    public String oneDriveKeyFileName() {
        return "onedrive.key";
    }

    @Bean
    public OneDriveProvider api(@Autowired String oneDriveKeyFileName) {
        File file = new File(getClass().getClassLoader().getResource(oneDriveKeyFileName).getFile());

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        } catch (CodeExpiredException cee) {
            throw new RuntimeException(cee);
        } catch (IOException e) {
            logger.error("OneDrive API init error", e);
        }

        return OneDriveProvider.FACTORY.readWriteApi(authoriser);
    }
}
