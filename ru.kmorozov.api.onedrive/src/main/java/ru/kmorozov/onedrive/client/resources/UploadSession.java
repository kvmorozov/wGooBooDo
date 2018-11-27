package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;

public class UploadSession {

    @Key
    private String uploadUrl;
    @Key
    private String[] nextExpectedRanges;

    public String getUploadUrl() {
        return this.uploadUrl;
    }

    public String[] getNextExpectedRanges() {
        return this.nextExpectedRanges;
    }
}
