package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;

public class ItemReference {

    @Key
    private String driveId;
    @Key
    private String id;
    @Key
    private String path;

    public String getDriveId() {
        return this.driveId;
    }

    public String getId() {
        return this.id;
    }

    public String getPath() {
        return this.path;
    }
}
