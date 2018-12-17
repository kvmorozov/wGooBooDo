package ru.kmorozov.gbd.core.logic.connectors.ok

import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
class OkResponse internal constructor(private val resp: okhttp3.Response) : Response {

    override val content: InputStream
        get() = resp.body()!!.byteStream()

    override val imageFormat: String
        get() = resp.body()!!.contentType()!!.subtype()

    override fun close() {
        resp.body()!!.close()
    }
}
