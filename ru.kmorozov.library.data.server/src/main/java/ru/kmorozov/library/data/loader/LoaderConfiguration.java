package ru.kmorozov.library.data.loader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.webdriver.WebDriverHttpConnector;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.OneDriveProvider.FACTORY;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import ru.kmorozov.onedrive.client.authoriser.TokenFactory;
import ru.kmorozov.onedrive.client.exceptions.InvalidCodeException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by km on 26.12.2016.
 */

@Configuration
public class LoaderConfiguration {

    private static final Logger logger = Logger.getLogger(LoaderConfiguration.class);

    @Value("${onedrive.key}")
    public String oneDriveKeyFileName;

    @Value("${webdriver.chrome.driver}")
    public String webdriverChromeDriverPath;

    @Value("${library.http.connector.type}")
    public String httpConnectorType;

    @Value("${onedrive.user}")
    public String oneDriveUser;

    @Value("${onedrive.password}")
    public String oneDrivePassword;

    @Value("${onedrive.clientId}")
    public String onedriveClientId;

    @Value("${onedrive.clientSecret}")
    public String onedriveClientSecret;

    @Bean
    public OneDriveProvider api() {
        URL keyResource = getClass().getClassLoader().getResource(oneDriveKeyFileName);

        if (keyResource == null) {
            logger.warn("Key file not found, creating new...");
        }

        final File keyFile = new File(keyResource == null ? oneDriveKeyFileName : keyResource.getFile());

        if (!keyFile.exists()) {
            try {
                keyFile.createNewFile();
            } catch (IOException e) {
                logger.error("Create keyFile error", e);
            }
        }

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), onedriveClientId, onedriveClientSecret);
        } catch (final InvalidCodeException cee) {
            System.setProperty("webdriver.chrome.driver", webdriverChromeDriverPath);
            if (TokenFactory.generateToken(keyFile, oneDriveUser, oneDrivePassword, onedriveClientId))
                try {
                    authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), onedriveClientId, onedriveClientSecret);
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

    @Bean
    @Lazy
    public HttpConnector getConnector() {
        switch (httpConnectorType) {
            case "chrome":
                System.setProperty("webdriver.chrome.driver", webdriverChromeDriverPath);
                return new WebDriverHttpConnector();
            case "google":
                return new GoogleHttpConnector();
            default:
                return new GoogleHttpConnector();
        }
    }
}
