package ru.kmorozov.onedrive.client.serialization

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class JsonDateSerializer {

    @Synchronized
    fun serialize(value: Date): String {
        return df.format(value)
    }

    @Synchronized
    @Throws(ParseException::class)
    fun deserialize(value: String): Date {
        try {
            return df.parse(value)
        } catch (e: ParseException) {
            try {
                return df2.parse(value)
            } catch (e1: ParseException) {
                throw e
            }

        }

    }

    companion object {

        val INSTANCE = JsonDateSerializer()
        private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        private val df2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        init {
            df.timeZone = TimeZone.getTimeZone("UTC")
            df2.timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
