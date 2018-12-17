package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key
import ru.kmorozov.onedrive.client.facets.QuotaFacet

class Drive {

    @Key
    val id: String? = null

    @Key
    val driveType: String? = null

    @Key
    val owner: IdentitySet? = null

    @Key
    val quota: QuotaFacet? = null
}
