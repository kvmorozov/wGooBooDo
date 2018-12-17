package ru.kmorozov.onedrive.client

import com.google.api.client.http.GenericUrl
import com.google.api.client.util.Key
import ru.kmorozov.onedrive.client.resources.Drive

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class OneDriveUrl(encodedUrl: String) : GenericUrl(encodedUrl) {

    @Key("\$skiptoken")
    private var token: String? = null

    fun setToken(token: String) {
        this.token = token
    }

    companion object {

        private const val rootUrl = "https://graph.microsoft.com/v1.0"

        fun defaultDrive(): OneDriveUrl {
            return OneDriveUrl("$rootUrl/drive")
        }

        fun driveRoot(): OneDriveUrl {
            return OneDriveUrl("$rootUrl/drive/root")
        }

        fun children(id: String): OneDriveUrl {
            return OneDriveUrl("$rootUrl/drive/items/$id/children")
        }

        fun putContent(id: String, name: String): OneDriveUrl {
            return OneDriveUrl(rootUrl + "/drive/items/" + id + ":/" + encode(name) + ":/content")
        }

        fun postMultiPart(id: String): OneDriveUrl {
            return OneDriveUrl("$rootUrl/drive/items/$id/children")
        }

        fun createUploadSession(id: String, name: String): OneDriveUrl {
            return OneDriveUrl(rootUrl + "/drive/items/" + id + ":/" + encode(name) + ":/upload.createSession")
        }

        fun getPath(path: String): OneDriveUrl {
            return OneDriveUrl(rootUrl + "/drive/root:/" + encode(path).replace("%5C", "/"))
        }

        fun item(id: String): GenericUrl {
            return OneDriveUrl("$rootUrl/drive/items/$id")
        }

        fun content(id: String): GenericUrl {
            return OneDriveUrl("$rootUrl/drive/items/$id/content")
        }

        fun search(drive: Drive, query: String): OneDriveUrl {
            return OneDriveUrl(rootUrl + "/drives/" + drive.id + "/root/search(q='{" + query + "}')")
        }

        private fun encode(url: String): String {
            return URLEncoder.encode(url, StandardCharsets.UTF_8)
        }
    }
}

