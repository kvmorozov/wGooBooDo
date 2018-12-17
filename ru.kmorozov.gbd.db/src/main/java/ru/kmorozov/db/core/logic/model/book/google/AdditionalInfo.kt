package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
internal class AdditionalInfo : Serializable {

    @SerializedName("[JsonBookInfo]")
    private val jsonBookInfo: JsonBookInfo? = null
}
