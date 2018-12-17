package ru.kmorozov.onedrive.client.utils

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
object JsonUtils {

    val JSON_FACTORY: JsonFactory = GsonFactory()
}
