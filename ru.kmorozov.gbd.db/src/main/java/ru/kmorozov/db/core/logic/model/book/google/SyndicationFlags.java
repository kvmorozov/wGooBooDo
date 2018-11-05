package ru.kmorozov.db.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class SyndicationFlags implements Serializable {

    @SerializedName("allow_disabling_chrome")
    private boolean allowDisablingChrome;
}
