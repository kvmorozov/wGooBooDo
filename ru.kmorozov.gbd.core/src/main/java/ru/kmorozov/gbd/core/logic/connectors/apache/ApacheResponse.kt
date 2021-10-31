package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.IOException
import java.io.InputStream

/**
 * Created by km on 20.05.2016.
 */
class ApacheResponse internal constructor(private val response: CloseableHttpResponse) : Response {
    override val statusCode: Int
        get() = response.statusLine.statusCode

    override val content: InputStream
        @Throws(IOException::class)
        get() = response.entity.content

    override val headers: String
        get() = response.allHeaders.toString()

    override val imageFormat: String
        get() {
            val contentType = response.entity.contentType.value

            return if (contentType.startsWith("image/")) contentType.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] else "unknown"
        }

    override fun close() {
        EntityUtils.consumeQuietly(response.entity)
    }
}
