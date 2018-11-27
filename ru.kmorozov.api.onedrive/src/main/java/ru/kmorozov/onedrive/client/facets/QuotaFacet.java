package ru.kmorozov.onedrive.client.facets;

import com.google.api.client.util.Key;

public class QuotaFacet {

    @Key
    private long total;
    @Key
    private long used;
    @Key
    private long remaining;
    @Key
    private long deleted;
    @Key
    private String state;

    public long getTotal() {
        return this.total;
    }

    public long getUsed() {
        return this.used;
    }

    public long getRemaining() {
        return this.remaining;
    }

    public long getDeleted() {
        return this.deleted;
    }

    public String getState() {
        return this.state;
    }
}
