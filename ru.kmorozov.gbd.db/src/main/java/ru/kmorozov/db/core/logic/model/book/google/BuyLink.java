package ru.kmorozov.db.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class BuyLink implements Serializable {

    @SerializedName("Seller")
    private String seller;
    @SerializedName("Url")
    private String url;
    @SerializedName("TrackingUrl")
    private String trackingUrl;
    @SerializedName("IsPublisher")
    private String publisher;
    @SerializedName("Price")
    private String price;
}
