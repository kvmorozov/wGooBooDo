package ru.kmorozov.gbd.core.logic.connectors.ktor

import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.jvm.javaio.toInputStream
import ru.kmorozov.gbd.core.logic.connectors.Response
import java.io.InputStream

class KtorResponse internal constructor(private val response: HttpResponse) : Response {

    override val content: InputStream
        get() = response.content.toInputStream()
    override val imageFormat: String
        get() = response.headers.get("content-type")?.split("/".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.get(1)!!
    override val headers: String
        get() = response.headers.toString()
    override val statusCode: Int
        get() = response.status.value

    override fun close() {
    }

}