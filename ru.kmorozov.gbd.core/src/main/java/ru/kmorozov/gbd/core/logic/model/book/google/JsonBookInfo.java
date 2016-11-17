package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class JsonBookInfo implements Serializable {

    @SerializedName("BuyLinks") private BuyLink[] buyLinks;
    @SerializedName("AboutUrl") private String aboutUrl;
    @SerializedName("PreviewUrl") private String previewUrl;
    @SerializedName("allowed_syndication_flags") private SyndicationFlags flags;
    @SerializedName("online_access") private OnlineAccess onlineAccess;
    @SerializedName("TocLine") private TocItem[] toc;
}
