package ru.kmorozov.gbd.core.logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
class SyndicationFlags implements Serializable {

    @JsonProperty("allow_disabling_chrome") private boolean allowDisablingChrome;
}
