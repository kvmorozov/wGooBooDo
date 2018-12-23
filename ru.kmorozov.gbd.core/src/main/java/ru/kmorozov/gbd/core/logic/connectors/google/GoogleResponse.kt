package ru.kmorozov.gbd.core.logic.connectors.google

import com.google.api.client.http.HttpResponse
import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.IOException
import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
internal class GoogleResponse(private val resp: HttpResponse) : Response {

    override val content: InputStream
        @Throws(IOException::class)
        get() = resp.content

    override val imageFormat: String
        get() = resp.mediaType.subType

    override fun close() {

    }
}
