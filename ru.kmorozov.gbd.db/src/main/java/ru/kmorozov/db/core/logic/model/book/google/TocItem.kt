package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
internal class TocItem : Serializable {

    @SerializedName("Title")
    private val title: String? = null
    @SerializedName("Pid")
    private val pid: String? = null
    @SerializedName("PgNum")
    private val pgNum: String? = null
    @SerializedName("Order")
    private val order: Int = 0
}
