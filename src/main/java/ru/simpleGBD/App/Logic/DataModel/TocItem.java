package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class TocItem {

    @JsonProperty("Title") private String title;
    @JsonProperty("Pid") private String pid;
    @JsonProperty("PgNum") private String pgNum;
}
