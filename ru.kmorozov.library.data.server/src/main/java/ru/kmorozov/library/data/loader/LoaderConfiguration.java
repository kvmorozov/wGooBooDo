package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.OneDriveProvider.FACTORY;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import com.wouterbreukink.onedrive.client.authoriser.TokenFactory;
import com.wouterbreukink.onedrive.client.exceptions.InvalidCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.config.MongoConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by km on 26.12.2016.
 */

@Configuration
@ComponentScan(basePackageClasses = MongoConfiguration.class, basePackages = "ru.kmorozov.library.data.loader")
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
    public OneDriveProvider api(@Autowired final String oneDriveKeyFileName) {
        final File file = new File(getClass().getClassLoader().getResource(oneDriveKeyFileName).getFile());

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        } catch (final InvalidCodeException cee) {
            if (TokenFactory.generateToken(oneDriveKeyFileName))
                try {
                    authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
                } catch (final IOException e) {
                    logger.error("OneDrive API init error", e);
                }
            else
                throw new RuntimeException(cee);
        } catch (final IOException e) {
            logger.error("OneDrive API init error", e);
        }

        return FACTORY.readWriteApi(authoriser);
    }
}
