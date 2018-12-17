package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 05.12.2015.
 */
internal class OnlineAccess : Serializable {

    @SerializedName("url")
    private val url: String? = null
    @SerializedName("price")
    private val price: String? = null
    @SerializedName("list_price")
    private val listPrice: String? = null
}
