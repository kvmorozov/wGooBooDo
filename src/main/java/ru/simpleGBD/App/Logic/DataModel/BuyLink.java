package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class BuyLink {

    @JsonProperty("Seller") private String seller;
    @JsonProperty("Url") private String url;
    @JsonProperty("TrackingUrl") private String trackingUrl;
    @JsonProperty("IsPublisher") private String publisher;
    @JsonProperty("Price") private String price;
}
