package ru.kmorozov.gbd.core.logic.connectors.webdriver

import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.FluentWait
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import java.io.IOException
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS
import java.time.temporal.ChronoUnit.SECONDS

class WebDriverHttpConnector : HttpConnector() {

    protected var driver: WebDriver? = null

    init {
        driver = ChromeDriver()
    }

    @Throws(IOException::class)
    override fun getContent(url: String, proxy: HttpHostExt, withTimeout: Boolean): Response? {
        driver!!.get(url)

        FluentWait(driver!!)
                .withTimeout(Duration.of(HttpConnector.CONNECT_TIMEOUT.toLong(), MILLIS))
                .pollingEvery(Duration.of(1, SECONDS))
                .ignoring(NoSuchElementException::class.java)

        return WebDriverResponse(driver!!.pageSource)
    }

    override fun close() {
        if (driver != null)
            driver!!.close()
    }
}
