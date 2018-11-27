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

    @Bean @Lazy
    public OneDriveProvider api() {
        final URL keyResource = this.getClass().getClassLoader().getResource(this.oneDriveKeyFileName);

        if (keyResource == null) {
            LoaderConfiguration.logger.warn("Key file not found, creating new...");
        }

        File keyFile = new File(keyResource == null ? this.oneDriveKeyFileName : keyResource.getFile());

        if (!keyFile.exists()) {
            try {
                keyFile.createNewFile();
            } catch (final IOException e) {
                LoaderConfiguration.logger.error("Create keyFile error", e);
            }
        }

        AuthorisationProvider authoriser = null;

        try {
            authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), this.onedriveClientId, this.onedriveClientSecret);
        } catch (InvalidCodeException cee) {
            System.setProperty("webdriver.chrome.driver", this.webdriverChromeDriverPath);
            if (TokenFactory.generateToken(keyFile, this.oneDriveUser, this.oneDrivePassword, this.onedriveClientId))
                try {
                    authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), this.onedriveClientId, this.onedriveClientSecret);
                } catch (IOException e) {
                    LoaderConfiguration.logger.error("OneDrive API init error", e);
                }
            else
                throw new RuntimeException(cee);
        } catch (IOException e) {
            LoaderConfiguration.logger.error("OneDrive API init error", e);
        }

        return OneDriveProvider.FACTORY.readWriteApi(authoriser);
    }

    @Bean
    @Lazy
    public HttpConnector getConnector() {
        switch (this.httpConnectorType) {
            case "chrome":
                System.setProperty("webdriver.chrome.driver", this.webdriverChromeDriverPath);
                return new WebDriverHttpConnector();
            case "google":
                return new GoogleHttpConnector();
            default:
                return new GoogleHttpConnector();
        }
    }
}
