package ru.kmorozov.gbd.core.logic.connectors.webdriver

import org.apache.commons.io.input.CharSequenceInputStream
import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

internal class WebDriverResponse(private val response: String) : Response {


    override val content: InputStream
        @Throws(IOException::class)
        get() = CharSequenceInputStream(response, Charset.forName("UTF-8"))

    override val imageFormat: String
        get() = "unknown"

    @Throws(IOException::class)
    override fun close() {

    }
}
