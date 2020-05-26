package ru.kmorozov.gbd.core.logic.connectors.asynchttp

import org.asynchttpclient.Response

import java.io.InputStream

/**
 * Created by km on 23.12.2016.
 */
class AsyncHttpResponse internal constructor(private val response: Response) : ru.kmorozov.gbd.core.logic.connectors.Response {
    override val statusCode: Int
        get() = response.statusCode

    override val content: InputStream
        get() = response.responseBodyAsStream

    override val headers: String
        get() = response.headers.toString()

    override val imageFormat: String
        get() {
            val rawContendType = response.contentType

            return if (rawContendType.startsWith("image/")) rawContendType.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] else "badFile"
        }

    override fun close() {

    }
}
