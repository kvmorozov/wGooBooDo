package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class JsonBookInfo implements Serializable {

    @JsonProperty("BuyLinks") private BuyLink[] buyLinks;
    @JsonProperty("AboutUrl") private String aboutUrl;
    @JsonProperty("PreviewUrl") private String previewUrl;
    @JsonProperty("allowed_syndication_flags") private SyndicationFlags flags;
    @JsonProperty("online_access") private OnlineAccess onlineAccess;
    @JsonProperty("TocLine") private TocItem[] toc;
}
