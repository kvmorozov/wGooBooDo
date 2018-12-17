package ru.kmorozov.onedrive.client.exceptions

import com.google.api.client.util.Key

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
class OneDriveErrorInfo {

    @Key("error")
    lateinit var error: String
    @Key("error_description")
    var description: String = ""

    override fun toString(): String {
        return "OneDriveErrorInfo{" +
                "error='" + error + '\''.toString() +
                ", description='" + description + '\''.toString() +
                '}'.toString()
    }
}
