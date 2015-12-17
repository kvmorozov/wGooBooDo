package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BuyLink implements Serializable {

    @JsonProperty("Seller") private String seller;
    @JsonProperty("Url") private String url;
    @JsonProperty("TrackingUrl") private String trackingUrl;
    @JsonProperty("IsPublisher") private String publisher;
    @JsonProperty("Price") private String price;
}
