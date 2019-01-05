package ru.kmorozov.gbd.core.logic.connectors

import java.io.Closeable
import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
interface Response : Closeable {

    val content: InputStream

    val imageFormat: String

    val statusCode: Int

    val empty: Boolean
        get() = this == EMPTY_RESPONSE

    companion object {
        val EMPTY_RESPONSE: Response = object : Response {
            override val statusCode: Int
                get() = 200
            override val content: InputStream
                get() = System.`in`;
            override val imageFormat: String
                get() = "unknown"

            override fun close() {

            }
        }
    }
}
