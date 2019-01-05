package ru.kmorozov.gbd.core.logic.connectors.ok

import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
class OkResponse internal constructor(private val response: okhttp3.Response) : Response {
    override val statusCode: Int
        get() = response.code()

    override val content: InputStream
        get() = response.body()!!.byteStream()

    override val imageFormat: String
        get() = response.body()!!.contentType()!!.subtype()

    override fun close() {
        response.body()!!.close()
    }
}
