package ru.kmorozov.gbd.core.logic.connectors.webdriver;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

public class WebDriverHttpConnector extends HttpConnector {

    protected WebDriver driver;

    public WebDriverHttpConnector() {
        driver = new ChromeDriver();
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        driver.get(url);

        final Wait<WebDriver> wait = new FluentWait(driver)
                .withTimeout(Duration.of(CONNECT_TIMEOUT, MILLIS))
                .pollingEvery(Duration.of(1, SECONDS))
                .ignoring(NoSuchElementException.class);

        return new WebDriverResponse(driver.getPageSource());
    }

    @Override
    public void close() {
        if (driver != null)
            driver.close();
    }
}
