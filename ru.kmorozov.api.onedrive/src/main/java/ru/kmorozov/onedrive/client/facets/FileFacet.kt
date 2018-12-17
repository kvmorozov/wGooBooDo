package ru.kmorozov.onedrive.client.facets

import com.google.api.client.util.Key

class FileFacet {

    @Key
    val mimeType: String? = null
    @Key
    val hashes: HashesFacet? = null
}
