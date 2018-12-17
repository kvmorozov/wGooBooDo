package ru.kmorozov.onedrive.client.facets

import com.google.api.client.util.Key

class ErrorFacet {

    @Key
    val code: String? = null
    @Key
    val message: String? = null
    @Key
    val innerError: ErrorFacet? = null
}
