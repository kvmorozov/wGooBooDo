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
        this.driver = new ChromeDriver();
    }

    @Override
    public Response getContent(final String url, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        this.driver.get(url);

        Wait<WebDriver> wait = new FluentWait(this.driver)
                .withTimeout(Duration.of(HttpConnector.CONNECT_TIMEOUT, MILLIS))
                .pollingEvery(Duration.of(1, SECONDS))
                .ignoring(NoSuchElementException.class);

        return new WebDriverResponse(this.driver.getPageSource());
    }

    @Override
    public void close() {
        if (this.driver != null)
            this.driver.close();
    }
}
