package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;
import ru.kmorozov.onedrive.client.facets.QuotaFacet;

public class Drive {

    @Key
    private String id;

    @Key
    private String driveType;

    @Key
    private IdentitySet owner;

    @Key
    private QuotaFacet quota;

    public String getId() {
        return this.id;
    }

    public String getDriveType() {
        return this.driveType;
    }

    public IdentitySet getOwner() {
        return this.owner;
    }

    public QuotaFacet getQuota() {
        return this.quota;
    }
}
