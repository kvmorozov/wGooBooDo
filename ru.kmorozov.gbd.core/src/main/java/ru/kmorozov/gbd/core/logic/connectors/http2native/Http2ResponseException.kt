package ru.kmorozov.gbd.core.logic.connectors.http2native

import ru.kmorozov.gbd.core.logic.connectors.ResponseException

import java.io.IOException

class Http2ResponseException internal constructor(ioe: IOException) : ResponseException(ioe) {

    override val statusCode: Int
        get() = 500
}
