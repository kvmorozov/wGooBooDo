package ru.kmorozov.gbd.core.logic.model.book.shpl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPage {

    @JsonProperty("id")
    private String id;
    @JsonProperty("w")
    private Integer width;
    @JsonProperty("h")
    private Integer height;
    @JsonProperty("downloadUrl")
    private String downloadUrl;
}
