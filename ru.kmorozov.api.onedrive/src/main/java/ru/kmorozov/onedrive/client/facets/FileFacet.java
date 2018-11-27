package ru.kmorozov.onedrive.client.facets;

import com.google.api.client.util.Key;

public class FileFacet {

    @Key
    private String mimeType;
    @Key
    private HashesFacet hashes;

    public String getMimeType() {
        return this.mimeType;
    }

    public HashesFacet getHashes() {
        return this.hashes;
    }
}
