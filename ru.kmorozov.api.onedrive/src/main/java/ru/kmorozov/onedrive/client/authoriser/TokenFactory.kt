package ru.kmorozov.onedrive.client.authoriser

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.FluentWait

import java.io.File
import java.io.PrintWriter
import java.time.Duration
import java.util.regex.Pattern

import java.time.temporal.ChronoUnit.MINUTES
import java.time.temporal.ChronoUnit.SECONDS

object TokenFactory {

    private val log = LogManager.getLogger(TokenFactory::class.java.name)

    fun generateToken(oneDriveKeyFile: File, user: String, password: String, clientId: String): Boolean {
        log.info("Starting generate token...")
        try {
            val driver = ChromeDriver()
            driver.get(OneDriveAuthorisationProvider.getAuthString(clientId))

            val wait = FluentWait(driver)
                    .withTimeout(Duration.of(30, MINUTES))
                    .pollingEvery(Duration.of(1, SECONDS))
                    .ignoring(NoSuchElementException::class.java)

            if (!StringUtils.isEmpty(user)) {
                val loginEdit = wait.until { driver1 -> driver1.findElement(By.name("loginfmt")) }
                loginEdit.sendKeys(user)
                val nextBtn = driver.findElement(By.className("btn-primary"))
                if (nextBtn != null)
                    try {
                        nextBtn.click()
                    } catch (ex: Exception) {
                        log.error("Failed generate token, continuing manually...", ex)
                    }

            }

            if (!StringUtils.isEmpty(password)) {
                val pwdEdit = wait.until { it.findElement(By.name("passwd")) }
                pwdEdit.sendKeys(password)
                val nextBtn = wait.until { driver.findElement(By.className("btn-primary")) }
                if (nextBtn != null)
                    try {
                        nextBtn.click()
                    } catch (ex: Exception) {
                        log.error("Failed generate token, continuing manually...", ex)
                    }

            }

            if (!Pattern.compile(OneDriveAuthorisationProvider.REDIRECT_URL + ".*code=(.*)").matcher(driver.currentUrl).matches()) {
                val okButton = wait.until { driver1 -> driver1.findElement(By.id("idBtn_Accept")) }
                try {
                    okButton.click()
                } catch (ex: Exception) {
                    log.error("Failed generate token, continuing manually...", ex)
                }

                wait.until<WebElement> { driver12 ->
                    try {
                        val button = driver12.findElement(By.id("idBtn_Accept"))
                        return@until null
                    } catch (nse: NoSuchElementException) {
                        return@until okButton
                    }
                }
            }

            PrintWriter(oneDriveKeyFile).use { writer -> writer.write(driver.currentUrl) }

            driver.close()
            driver.quit()
            log.info("Token generated successfully!")

            return true
        } catch (ex: Exception) {
            log.error("Failed generate token!", ex)
            return false
        }

    }
}
