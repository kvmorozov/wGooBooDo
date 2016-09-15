package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class TocItem implements Serializable {

    @JsonProperty("Title") private String title;
    @JsonProperty("Pid") private String pid;
    @JsonProperty("PgNum") private String pgNum;
    @JsonProperty("Order") private int order;
}
