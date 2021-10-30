package ru.kmorozov.gbd.core.logic.connectors.http2native

import ru.kmorozov.gbd.core.logic.connectors.Response
import java.io.IOException
import java.io.InputStream
import java.net.http.HttpResponse

class Http2Response internal constructor(private val response: HttpResponse<*>) : Response {
    override val statusCode: Int
        get() = response.statusCode()

    override val content: InputStream
        get() = response.body() as InputStream

    override val imageFormat: String
        get() = response.headers().firstValue("content-type").get().split("/".toRegex()).dropLastWhile { it.isEmpty }.toTypedArray()[1]

    override val headers: String
        get() = response.headers().toString()

    @Throws(IOException::class)
    override fun close() {
        content.close()
    }
}
