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

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
