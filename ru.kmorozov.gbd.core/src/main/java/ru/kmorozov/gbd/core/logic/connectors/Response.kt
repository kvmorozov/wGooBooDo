package ru.kmorozov.gbd.core.logic.connectors

import org.apache.commons.io.input.NullInputStream
import java.io.Closeable
import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
interface Response : Closeable {

    val content: InputStream

    val imageFormat: String

    companion object {
        val EMPTY_RESPONCE: Response = object : Response {
            override val content: InputStream
                get() = NullInputStream(0);
            override val imageFormat: String
                get() = "unknown"

            override fun close() {

            }
        }
    }
}
