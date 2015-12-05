package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 05.12.2015.
 */
public class OnlineAccess {

    @JsonProperty("url") private String url;
    @JsonProperty("price") private String price;
    @JsonProperty("list_price") private String listPrice;
}
