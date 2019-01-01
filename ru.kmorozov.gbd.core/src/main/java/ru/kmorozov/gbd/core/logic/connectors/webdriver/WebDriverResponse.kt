package ru.kmorozov.gbd.core.logic.connectors.webdriver

import ru.kmorozov.gbd.core.logic.connectors.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

internal class WebDriverResponse(private val response: String) : Response {


    override val content: InputStream
        @Throws(IOException::class)
        get() = ByteArrayInputStream(response.toByteArray(StandardCharsets.UTF_8));

    override val imageFormat: String
        get() = "unknown"

    @Throws(IOException::class)
    override fun close() {

    }
}
