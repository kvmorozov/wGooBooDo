package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key
import ru.kmorozov.onedrive.client.facets.ErrorFacet

class ErrorSet {

    @Key
    val error: ErrorFacet? = null
}
