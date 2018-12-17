package ru.kmorozov.onedrive.client.facets

import com.google.api.client.util.Key

class QuotaFacet {

    @Key
    val total: Long = 0
    @Key
    val used: Long = 0
    @Key
    val remaining: Long = 0
    @Key
    val deleted: Long = 0
    @Key
    val state: String? = null
}
