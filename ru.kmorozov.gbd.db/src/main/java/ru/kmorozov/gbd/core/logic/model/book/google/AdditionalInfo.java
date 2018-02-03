package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class AdditionalInfo implements Serializable {

    @SerializedName("[JsonBookInfo]")
    private JsonBookInfo jsonBookInfo;
}
