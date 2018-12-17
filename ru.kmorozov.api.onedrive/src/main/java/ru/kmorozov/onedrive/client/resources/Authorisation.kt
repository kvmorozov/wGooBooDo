package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key

class Authorisation {

    @Key("token_type")
    val tokenType: String? = null
    @Key("expires_in")
    val expiresIn: Int = 0
    @Key("scope")
    val scope: String? = null
    @Key("access_token")
    val accessToken: String? = null
    @Key("refresh_token")
    val refreshToken: String? = null
    @Key("user_id")
    val userId: String? = null
    @Key("error")
    val error: String? = null
    @Key("error_description")
    val errorDescription: String? = null
}
