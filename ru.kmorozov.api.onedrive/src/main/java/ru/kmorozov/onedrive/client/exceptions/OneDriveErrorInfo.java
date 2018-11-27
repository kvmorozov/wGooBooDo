package ru.kmorozov.onedrive.client.exceptions;

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
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OneDriveErrorInfo{" +
                "error='" + this.error + '\'' +
                ", description='" + this.description + '\'' +
                '}';
    }
}
