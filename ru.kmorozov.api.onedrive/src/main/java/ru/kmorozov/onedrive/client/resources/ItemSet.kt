package ru.kmorozov.onedrive.client.resources

import com.google.api.client.util.Key
import java.util.regex.Pattern

class ItemSet {

    @Key
    val value: Array<Item>? = null
    @Key("@odata.nextLink")
    private val nextPage: String? = null

    // Create a Pattern object
    // Now create matcher object.
    val nextToken: String?
        get() {

            if (null == nextPage) {
                return null
            }

            val pattern = ".*skiptoken=(.*)"
            val r = Pattern.compile(pattern)
            val m = r.matcher(nextPage)
            return if (m.find()) {
                m.group(1)
            } else {
                throw Error("Unable to find page token")
            }
        }
}
