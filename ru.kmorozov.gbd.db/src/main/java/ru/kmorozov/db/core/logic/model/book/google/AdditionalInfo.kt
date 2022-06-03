package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
class AdditionalInfo : Serializable {

    @Expose
    @SerializedName("[JsonBookInfo]")
    val jsonBookInfo: JsonBookInfo? = null
}
