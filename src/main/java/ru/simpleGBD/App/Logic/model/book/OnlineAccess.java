package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 05.12.2015.
 */
class OnlineAccess implements Serializable {

    @JsonProperty("url") private String url;
    @JsonProperty("price") private String price;
    @JsonProperty("list_price") private String listPrice;
}
