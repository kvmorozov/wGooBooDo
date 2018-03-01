package com.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;
import com.kmorozov.onedrive.client.facets.ErrorFacet;

public class ErrorSet {

    @Key
    private ErrorFacet error;

    public ErrorFacet getError() {
        return error;
    }
}
