package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 05.12.2015.
 */
class OnlineAccess implements Serializable {

    @SerializedName("url") private String url;
    @SerializedName("price") private String price;
    @SerializedName("list_price") private String listPrice;
}
