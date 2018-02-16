package com.wouterbreukink.onedrive.client.authoriser;

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

    public static boolean generateToken(final File oneDriveKeyFile) {
        log.info("Starting generate token...");
        try {
            final WebDriver driver = new ChromeDriver();
            driver.get(OneDriveAuthorisationProvider.getAuthString());

            final Wait<WebDriver> wait = new FluentWait(driver)
                    .withTimeout(30, TimeUnit.MINUTES)
                    .pollingEvery(1, TimeUnit.SECONDS)
                    .ignoring(NoSuchElementException.class);

            final WebElement okButton = wait.until(driver1 -> driver1.findElement(By.id("idBtn_Accept")));

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
            log.info("Failed generate token!");
            return false;
        }
    }
}
