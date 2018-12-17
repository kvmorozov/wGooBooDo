package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key

class IdentitySet {

    @Key
    val user: Identity? = null
    @Key
    val application: Identity? = null
    @Key
    val device: Identity? = null
}
