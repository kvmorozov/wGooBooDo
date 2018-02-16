package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.OneDriveProvider.FACTORY;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import com.wouterbreukink.onedrive.client.authoriser.TokenFactory;
import com.wouterbreukink.onedrive.client.exceptions.InvalidCodeException;
import com.wouterbreukink.onedrive.client.exceptions.OneDriveException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.config.MongoConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by km on 26.12.2016.
 */

@Configuration
@ComponentScan(basePackageClasses = MongoConfiguration.class, basePackages = "ru.kmorozov.library.data.loader")
public class LoaderConfiguration {

    private static final Logger logger = Logger.getLogger(LoaderConfiguration.class);

    @Value("${onedrive.key}")
    public String oneDriveKeyFileName;

    @Value("${webdriver.chrome.driver}")
    public String webdriverChromeDriverPath;

    @Bean
    public OneDriveProvider api() throws OneDriveException {
        URL keyResource = getClass().getClassLoader().getResource(oneDriveKeyFileName);

        if (keyResource == null) {
            logger.warn("Key file not found, creating new...");
        }

        final File keyFile = new File(keyResource == null ? oneDriveKeyFileName : keyResource.getFile() );

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath());
        } catch (final InvalidCodeException cee) {
            System.setProperty("webdriver.chrome.driver", webdriverChromeDriverPath);
            if (TokenFactory.generateToken(keyFile))
                try {
                    authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath());
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
