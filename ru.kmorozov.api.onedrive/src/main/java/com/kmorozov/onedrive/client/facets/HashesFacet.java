package com.kmorozov.onedrive.client.facets;

import com.google.api.client.util.Key;

public class HashesFacet {

    @Key
    private String sha1Hash;
    @Key
    private String crc32Hash;

    public String getSha1Hash() {
        return sha1Hash;
    }

    public String getCrc32Hash() {
        return crc32Hash;
    }

    public long getCrc32() {
        // OneDrive does not always return a hash
        if (null == crc32Hash) {
            return 0;
        }

        final String reversed = crc32Hash.substring(6, 8) + crc32Hash.substring(4, 6) + crc32Hash.substring(2, 4) + crc32Hash.substring(0, 2);
        return Long.decode("0x" + reversed);
    }
}
