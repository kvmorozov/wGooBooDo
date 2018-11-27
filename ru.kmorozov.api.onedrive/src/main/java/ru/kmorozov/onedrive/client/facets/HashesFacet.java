package ru.kmorozov.onedrive.client.facets;

import com.google.api.client.util.Key;

public class HashesFacet {

    @Key
    private String sha1Hash;
    @Key
    private String crc32Hash;

    public String getSha1Hash() {
        return this.sha1Hash;
    }

    public String getCrc32Hash() {
        return this.crc32Hash;
    }

    public long getCrc32() {
        // OneDrive does not always return a hash
        if (null == this.crc32Hash) {
            return 0L;
        }

        String reversed = this.crc32Hash.substring(6, 8) + this.crc32Hash.substring(4, 6) + this.crc32Hash.substring(2, 4) + this.crc32Hash.substring(0, 2);
        return Long.decode("0x" + reversed);
    }
}
