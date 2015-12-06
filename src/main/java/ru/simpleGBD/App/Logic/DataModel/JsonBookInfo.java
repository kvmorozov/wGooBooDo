package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class JsonBookInfo {

    @JsonProperty("BuyLinks") private BuyLink[] buyLinks;
    @JsonProperty("AboutUrl") private String aboutUrl;
    @JsonProperty("PreviewUrl") private String previewUrl;
    @JsonProperty("allowed_syndication_flags") private SyndicationFlags flags;
    @JsonProperty("online_access") private OnlineAccess onlineAccess;
    @JsonProperty("TocLine") private TocItem[] toc;
}
