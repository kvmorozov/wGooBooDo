package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class AdditionalInfo implements Serializable {

    @JsonProperty("[JsonBookInfo]") private JsonBookInfo jsonBookInfo;
}
