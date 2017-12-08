package com.wouterbreukink.onedrive.client.exceptions;

import com.google.api.client.util.Key;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveErrorInfo {

    @Key("error")
    String error;
    @Key("error_description")
    String description;

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OneDriveErrorInfo{" +
                "error='" + error + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
