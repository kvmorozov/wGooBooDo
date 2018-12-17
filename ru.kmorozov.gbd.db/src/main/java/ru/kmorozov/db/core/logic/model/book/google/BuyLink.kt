package ru.kmorozov.db.core.logic.model.book.google

import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Created by km on 28.11.2015.
 */
internal class BuyLink : Serializable {

    @SerializedName("Seller")
    private val seller: String? = null
    @SerializedName("Url")
    private val url: String? = null
    @SerializedName("TrackingUrl")
    private val trackingUrl: String? = null
    @SerializedName("IsPublisher")
    private val publisher: String? = null
    @SerializedName("Price")
    private val price: String? = null
}
