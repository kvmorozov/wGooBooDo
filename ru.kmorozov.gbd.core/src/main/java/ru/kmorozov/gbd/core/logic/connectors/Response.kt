package ru.kmorozov.gbd.core.logic.connectors

import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * Created by km on 17.05.2016.
 */
interface Response : Closeable {

    val content: InputStream

    val imageFormat: String
}
