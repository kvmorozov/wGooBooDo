package ru.kmorozov.gbd.core.logic.connectors.http2native

import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.http.HttpResponse

class Http2Response internal constructor(private val response: HttpResponse<*>) : Response {
    override val content: InputStream

    override val imageFormat: String
        get() = response.headers().firstValue("content-type").get().split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

    init {
        content = ByteArrayInputStream(response.body() as ByteArray)
    }

    @Throws(IOException::class)
    override fun close() {
        content.close()
    }
}
