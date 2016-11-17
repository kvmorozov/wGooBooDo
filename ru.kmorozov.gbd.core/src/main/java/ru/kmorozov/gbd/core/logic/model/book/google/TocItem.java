package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class TocItem implements Serializable {

    @SerializedName("Title") private String title;
    @SerializedName("Pid") private String pid;
    @SerializedName("PgNum") private String pgNum;
    @SerializedName("Order") private int order;
}
