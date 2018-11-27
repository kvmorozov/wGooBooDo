package ru.kmorozov.onedrive.client.facets;

import com.google.api.client.util.Key;

public class ErrorFacet {

    @Key
    private String code;
    @Key
    private String message;
    @Key
    private ErrorFacet innerError;

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public ErrorFacet getInnerError() {
        return this.innerError;
    }
}
