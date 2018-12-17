package ru.kmorozov.onedrive.client.facets

import com.google.api.client.util.Key

class HashesFacet {

    @Key
    val sha1Hash: String? = null
    @Key
    val crc32Hash: String? = null

    // OneDrive does not always return a hash
    val crc32: Long
        get() {
            if (null == crc32Hash) {
                return 0L
            }

            val reversed = crc32Hash.substring(6, 8) + crc32Hash.substring(4, 6) + crc32Hash.substring(2, 4) + crc32Hash.substring(0, 2)
            return java.lang.Long.decode("0x$reversed")
        }
}
