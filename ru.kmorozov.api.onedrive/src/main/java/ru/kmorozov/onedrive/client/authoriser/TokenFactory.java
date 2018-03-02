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
import java.util.concurrent.TimeUnit;

public class TokenFactory {

    private static final Logger log = LogManager.getLogger(TokenFactory.class.getName());

    public static boolean generateToken(final File oneDriveKeyFile, final String user, final String password) {
        log.info("Starting generate token...");
        try {
            final WebDriver driver = new ChromeDriver();
            driver.get(OneDriveAuthorisationProvider.getAuthString());

            final Wait<WebDriver> wait = new FluentWait(driver)
                    .withTimeout(30, TimeUnit.MINUTES)
                    .pollingEvery(1, TimeUnit.SECONDS)
                    .ignoring(NoSuchElementException.class);

            if (!StringUtils.isEmpty(user)) {
                final WebElement loginEdit = wait.until(driver1 -> driver1.findElement(By.name("loginfmt")));
                loginEdit.sendKeys(user);
                final WebElement nextBtn = driver.findElement(By.className("btn-primary"));
                if (nextBtn != null)
                    try {
                        nextBtn.click();
                    } catch (Exception ex) {
                        log.error("Failed generate token, continuing manually...", ex);
                    }
            }

            if (!StringUtils.isEmpty(password)) {
                final WebElement pwdEdit = wait.until(driver1 -> driver1.findElement(By.name("passwd")));
                pwdEdit.sendKeys(password);
                final WebElement nextBtn = driver.findElement(By.className("btn-primary"));
                if (nextBtn != null)
                    try {
                        nextBtn.click();
                    } catch (Exception ex) {
                        log.error("Failed generate token, continuing manually...", ex);
                    }
            }

            final WebElement okButton = wait.until(driver1 -> driver1.findElement(By.id("idBtn_Accept")));
            try {
                okButton.click();
            } catch (Exception ex) {
                log.error("Failed generate token, continuing manually...", ex);
            }

            final WebElement noButton = wait.until(driver12 -> {
                try {
                    final WebElement button = driver12.findElement(By.id("idBtn_Accept"));
                    return null;
                } catch (final NoSuchElementException nse) {
                    return okButton;
                }
            });

            try (PrintWriter writer = new PrintWriter(oneDriveKeyFile)) {
                writer.write(driver.getCurrentUrl());
            }

            driver.close();
            log.info("Token generated successfully!");

            return true;
        } catch (final Exception ex) {
            log.error("Failed generate token!", ex);
            return false;
        }
    }

    public static boolean generateToken(final File oneDriveKeyFile) {
        return generateToken(oneDriveKeyFile, null, null);
    }
}
