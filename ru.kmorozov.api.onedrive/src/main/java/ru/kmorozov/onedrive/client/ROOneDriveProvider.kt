package ru.kmorozov.onedrive.client

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.Lists
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener
import ru.kmorozov.onedrive.client.resources.Drive
import ru.kmorozov.onedrive.client.resources.Item
import ru.kmorozov.onedrive.client.resources.ItemSet
import ru.kmorozov.onedrive.client.utils.JsonUtils

import java.io.File
import java.io.IOException
import java.net.SocketException
import java.util.Date

internal open class ROOneDriveProvider(authoriser: AuthorisationProvider) : OneDriveProvider {

    protected val requestFactory: HttpRequestFactory

    private var cachedDefaultDrive: Drive? = null

    protected val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()

    override val defaultDrive: Drive
        @Throws(IOException::class)
        get() {
            if (cachedDefaultDrive == null) {
                val request = requestFactory.buildGetRequest(OneDriveUrl.defaultDrive())
                cachedDefaultDrive = request.execute().parseAs(Drive::class.java)
            }

            return cachedDefaultDrive!!
        }

    override val root: OneDriveItem
        @Throws(IOException::class)
        get() {
            val request = requestFactory.buildGetRequest(OneDriveUrl.driveRoot())
            val response = request.execute().parseAs(Item::class.java)
            return OneDriveItem.FACTORY.create(response)
        }

    init {
        requestFactory = HTTP_TRANSPORT.createRequestFactory { request ->
            request.parser = JsonObjectParser(JsonUtils.JSON_FACTORY)
            request.readTimeout = 60000
            request.connectTimeout = 60000
            request.numberOfRetries = 5
            try {
                request.headers.authorization = "bearer " + authoriser.accessToken
            } catch (e: IOException) {
                throw e
            }

            request.unsuccessfulResponseHandler = OneDriveResponseHandler(authoriser)
        }
    }

    @Throws(IOException::class)
    override fun getChildren(parent: OneDriveItem): Array<OneDriveItem> {
        if (!parent.isDirectory) {
            throw IllegalArgumentException("Specified Item is not a folder")
        }

        val itemsToReturn = Lists.newArrayList<OneDriveItem>()

        var token: String? = null

        do {

            val url = OneDriveUrl.children(parent.id!!)

            if (null != token) {
                url.setToken(token)
            }

            val request = requestFactory.buildGetRequest(url)
            val response: HttpResponse

            try {
                response = request.execute()
            } catch (se: SocketException) {
                return arrayOf()
            }

            val items = response.parseAs(ItemSet::class.java)

            for (i in items.value!!) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i))
            }

            token = items.nextToken

        } while (null != token) // If we have a token for the next page we need to keep going

        return itemsToReturn.toTypedArray()
    }

    @Throws(IOException::class)
    override fun getChildren(id: String): Array<OneDriveItem> {
        val itemsToReturn = Lists.newArrayList<OneDriveItem>()

        var token: String? = null

        do {

            val url = OneDriveUrl.children(id)

            if (null != token) {
                url.setToken(token)
            }

            val request = requestFactory.buildGetRequest(url)
            val items = request.execute().parseAs(ItemSet::class.java)

            for (i in items.value!!) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i))
            }

            token = items.nextToken

        } while (null != token) // If we have a token for the next page we need to keep going

        return itemsToReturn.toTypedArray()
    }

    @Throws(IOException::class)
    override fun getPath(path: String): OneDriveItem {
        try {
            val request = requestFactory.buildGetRequest(OneDriveUrl.getPath(path))
            val response = request.execute().parseAs(Item::class.java)
            return OneDriveItem.FACTORY.create(response)
        } catch (e: HttpResponseException) {
            throw OneDriveAPIException(e.statusCode, "Unable to get path", e)
        } catch (e: IOException) {
            throw OneDriveAPIException(0, "Unable to get path", e)
        }

    }

    @Throws(IOException::class)
    override fun getItem(id: String): OneDriveItem {
        val request = requestFactory.buildGetRequest(OneDriveUrl.item(id))
        request.retryOnExecuteIOException = true
        request.ioExceptionHandler = ResumableDownloader.ioExceptionHandler
        val response = request.execute().parseAs(Item::class.java)
        return OneDriveItem.FACTORY.create(response)
    }

    @Throws(IOException::class)
    override fun replaceFile(parent: OneDriveItem, file: File): OneDriveItem {

        if (!parent.isDirectory) {
            throw IllegalArgumentException("Parent is not a folder")
        }

        return OneDriveItem.FACTORY.create(parent, file.name, file.isDirectory)
    }

    @Throws(IOException::class)
    override fun uploadFile(parent: OneDriveItem, file: File): OneDriveItem {

        if (!parent.isDirectory) {
            throw IllegalArgumentException("Parent is not a folder")
        }

        return OneDriveItem.FACTORY.create(parent, file.name, file.isDirectory)
    }

    @Throws(IOException::class)
    override fun startUploadSession(parent: OneDriveItem, file: File): OneDriveUploadSession {
        return OneDriveUploadSession(parent, file, "", EMPTY_STR_ARR)
    }

    @Throws(IOException::class)
    override fun uploadChunk(session: OneDriveUploadSession) {
        session.setComplete(OneDriveItem.FACTORY.create(session.parent, session.file.name, session.file.isDirectory))
    }

    @Throws(IOException::class)
    override fun updateFile(item: OneDriveItem, createdDate: Date, modifiedDate: Date): OneDriveItem {
        // Do nothing, just return the unmodified item
        return item
    }

    @Throws(IOException::class)
    override fun createFolder(parent: OneDriveItem, name: String): OneDriveItem {
        // Return a dummy folder
        return OneDriveItem.FACTORY.create(parent, name, true)
    }

    @Throws(IOException::class)
    override fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener, chunkSize: Int) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun delete(remoteFile: OneDriveItem) {
        // Do nothing
    }

    @Throws(IOException::class)
    override fun search(query: String): Array<OneDriveItem> {
        val itemsToReturn = Lists.newArrayList<OneDriveItem>()

        var token: String? = null

        do {

            val url = OneDriveUrl.search(defaultDrive, query)

            if (null != token) {
                url.setToken(token)
            }

            val request = requestFactory.buildGetRequest(url)
            val items = request.execute().parseAs(ItemSet::class.java)

            for (i in items.value!!) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i))
            }

            token = items.nextToken

        } while (null != token) // If we have a token for the next page we need to keep going

        return itemsToReturn.toTypedArray()
    }

    companion object {



        private val EMPTY_STR_ARR = arrayOf<String>()
    }
}
