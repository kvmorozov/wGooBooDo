package ru.kmorozov.gbd.core.logic.connectors.google

import com.google.api.client.http.HttpResponse
import ru.kmorozov.gbd.core.logic.connectors.Response

import java.io.IOException
import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
internal class GoogleResponse(private val response: HttpResponse) : Response {
    override val statusCode: Int
        get() = response.statusCode

    override val content: InputStream
        @Throws(IOException::class)
        get() = response.content

    override val imageFormat: String
        get() = response.mediaType.subType

    override fun close() {

    }
}
