package ru.kmorozov.onedrive.client.exceptions

import ru.kmorozov.onedrive.client.utils.JsonUtils

import java.io.IOException

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
object OneDriveExceptionFactory {

    fun getException(content: String): OneDriveException {
        var errorInfo: OneDriveErrorInfo? = null

        try {
            errorInfo = JsonUtils.JSON_FACTORY.fromString(content, OneDriveErrorInfo::class.java)
            when (errorInfo!!.error) {
                "invalid_grant", "server_error" -> return InvalidCodeException(errorInfo)
                else -> return OneDriveException(errorInfo)
            }
        } catch (ignored: IOException) {
        }

        return OneDriveException(errorInfo)
    }
}
