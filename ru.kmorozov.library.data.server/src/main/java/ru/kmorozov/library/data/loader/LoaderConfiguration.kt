package ru.kmorozov.library.data.loader

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.OneDriveProvider.FACTORY
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import ru.kmorozov.onedrive.client.authoriser.TokenFactory
import ru.kmorozov.onedrive.client.exceptions.InvalidCodeException
import java.io.File
import java.io.IOException

/**
 * Created by km on 26.12.2016.
 */

@Configuration
@ComponentScan(basePackageClasses = arrayOf(OneDriveProvider::class))
open class LoaderConfiguration {

    @Value("\${onedrive.key}")
    var oneDriveKeyFileName: String? = null

    @Value("\${webdriver.chrome.driver}")
    var webdriverChromeDriverPath: String? = null

    @Value("\${library.http.connector.type}")
    var httpConnectorType: String? = null

    @Value("\${onedrive.user}")
    var oneDriveUser: String? = null

    @Value("\${onedrive.password}")
    var oneDrivePassword: String? = null

    @Value("\${onedrive.clientId}")
    var onedriveClientId: String? = null

    @Value("\${onedrive.clientSecret}")
    var onedriveClientSecret: String? = null

    open val connector: HttpConnector
        @Bean
        @Lazy
        get() {
            when (httpConnectorType) {
                "google" -> return GoogleHttpConnector()
                else -> return GoogleHttpConnector()
            }
        }

    @Bean
    @Lazy
    open fun api(): OneDriveProvider {
        val keyResource = javaClass.classLoader.getResource(oneDriveKeyFileName!!)

        if (keyResource == null) {
            logger.warn("Key file not found, creating new...")
        }

        val keyFile = File(if (keyResource == null) oneDriveKeyFileName else keyResource.file)

        if (!keyFile.exists()) {
            try {
                keyFile.createNewFile()
            } catch (e: IOException) {
                logger.error("Create keyFile error", e)
            }

        }

        var authoriser: AuthorisationProvider? = null

        try {
            authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), onedriveClientId!!, onedriveClientSecret!!)
        } catch (cee: InvalidCodeException) {
            System.setProperty("webdriver.chrome.driver", webdriverChromeDriverPath!!)
            if (TokenFactory.generateToken(keyFile, oneDriveUser!!, oneDrivePassword!!, onedriveClientId!!))
                try {
                    authoriser = AuthorisationProvider.FACTORY.create(keyFile.toPath(), onedriveClientId!!, onedriveClientSecret!!)
                } catch (e: IOException) {
                    logger.error("OneDrive API init error", e)
                }
            else
                throw RuntimeException(cee)
        } catch (e: IOException) {
            logger.error("OneDrive API init error", e)
        }

        return FACTORY.readWriteApi(authoriser!!)
    }

    companion object {

        private val logger = Logger.getLogger(LoaderConfiguration::class.java)
    }
}
