package com.wouterbreukink.onedrive.client.exceptions;

import com.google.api.client.util.Key;
import com.google.gson.annotations.SerializedName;

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

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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
