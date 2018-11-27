package ru.kmorozov.onedrive.client.authoriser;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.io.File;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

public class TokenFactory {

    private static final Logger log = LogManager.getLogger(TokenFactory.class.getName());

    public static boolean generateToken(File oneDriveKeyFile, String user, String password, final String clientId) {
        TokenFactory.log.info("Starting generate token...");
        try {
            WebDriver driver = new ChromeDriver();
            driver.get(OneDriveAuthorisationProvider.getAuthString(clientId));

            Wait<WebDriver> wait = new FluentWait(driver)
                    .withTimeout(Duration.of(30, MINUTES))
                    .pollingEvery(Duration.of(1, SECONDS))
                    .ignoring(NoSuchElementException.class);

            if (!StringUtils.isEmpty(user)) {
                WebElement loginEdit = wait.until(driver1 -> driver1.findElement(By.name("loginfmt")));
                loginEdit.sendKeys(user);
                WebElement nextBtn = driver.findElement(By.className("btn-primary"));
                if (nextBtn != null)
                    try {
                        nextBtn.click();
                    } catch (final Exception ex) {
                        TokenFactory.log.error("Failed generate token, continuing manually...", ex);
                    }
            }

            if (!StringUtils.isEmpty(password)) {
                WebElement pwdEdit = wait.until(driver1 -> driver1.findElement(By.name("passwd")));
                pwdEdit.sendKeys(password);
                WebElement nextBtn = wait.until(driver1 -> driver.findElement(By.className("btn-primary")));
                if (nextBtn != null)
                    try {
                        nextBtn.click();
                    } catch (final Exception ex) {
                        TokenFactory.log.error("Failed generate token, continuing manually...", ex);
                    }
            }

            if (!Pattern.compile(OneDriveAuthorisationProvider.REDIRECT_URL + ".*code=(.*)").matcher(driver.getCurrentUrl()).matches()) {
                WebElement okButton = wait.until(driver1 -> driver1.findElement(By.id("idBtn_Accept")));
                try {
                    okButton.click();
                } catch (final Exception ex) {
                    TokenFactory.log.error("Failed generate token, continuing manually...", ex);
                }

                WebElement noButton = wait.until(driver12 -> {
                    try {
                        WebElement button = driver12.findElement(By.id("idBtn_Accept"));
                        return null;
                    } catch (NoSuchElementException nse) {
                        return okButton;
                    }
                });
            }

            try (final PrintWriter writer = new PrintWriter(oneDriveKeyFile)) {
                writer.write(driver.getCurrentUrl());
            }

            driver.close();
            driver.quit();
            TokenFactory.log.info("Token generated successfully!");

            return true;
        } catch (Exception ex) {
            TokenFactory.log.error("Failed generate token!", ex);
            return false;
        }
    }
}
