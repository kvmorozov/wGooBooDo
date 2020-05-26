package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.core5.http.io.entity.EntityUtils
import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.IOException
import java.io.InputStream

/**
 * Created by km on 20.05.2016.
 */
class ApacheResponse internal constructor(private val response: CloseableHttpResponse) : Response {
    override val statusCode: Int
        get() = response.code

    override val content: InputStream
        @Throws(IOException::class)
        get() = response.entity.content

    override val headers: String
        get() = response.headers.toString()

    override val imageFormat: String
        get() {
            val contentType = response.entity.contentType

            return if (contentType.startsWith("image/")) contentType.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] else "unknown"
        }

    override fun close() {
        EntityUtils.consumeQuietly(response.entity)
    }
}
