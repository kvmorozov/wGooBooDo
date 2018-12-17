package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key

class UploadSession {

    @Key
    val uploadUrl: String? = null
    @Key
    val nextExpectedRanges: Array<String>? = null
}
