package ru.kmorozov.gbd.core.logic.connectors

import java.io.IOException

/**
 * Created by km on 17.05.2016.
 */
abstract class ResponseException(ex: IOException) : IOException(ex) {

    abstract val statusCode: Int
}
