package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
class TocItem : Serializable {

    @Expose @SerializedName("Title")
    val title: String? = null
    @Expose @SerializedName("Pid")
    val pid: String? = null
    @Expose @SerializedName("PgNum")
    val pgNum: String? = null
    @Expose @SerializedName("Order")
    val order: Int = 0
}
