package ru.kmorozov.onedrive.client.facets

import com.google.api.client.util.Key

class FileSystemInfoFacet {

    @Key
    var createdDateTime: String? = null
    @Key
    var lastModifiedDateTime: String? = null
}
