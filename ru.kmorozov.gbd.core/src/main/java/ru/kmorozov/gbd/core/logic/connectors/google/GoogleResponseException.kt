package ru.kmorozov.gbd.core.logic.connectors.google

import com.google.api.client.http.HttpResponseException
import ru.kmorozov.gbd.core.logic.connectors.ResponseException

/**
 * Created by km on 17.05.2016.
 */
internal class GoogleResponseException(private val hre: HttpResponseException) : ResponseException(hre) {

    override val statusCode: Int
        get() = hre.statusCode
}
