package ru.kmorozov.onedrive.client

import com.google.api.client.http.*
import com.google.api.client.http.MultipartContent.Part
import com.google.api.client.http.json.JsonHttpContent
import com.google.api.client.util.IOUtils
import com.google.api.client.util.Key
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener
import ru.kmorozov.onedrive.client.facets.FileFacet
import ru.kmorozov.onedrive.client.facets.FileSystemInfoFacet
import ru.kmorozov.onedrive.client.facets.FolderFacet
import ru.kmorozov.onedrive.client.resources.Item
import ru.kmorozov.onedrive.client.resources.UploadSession
import ru.kmorozov.onedrive.client.serialization.JsonDateSerializer
import ru.kmorozov.onedrive.client.utils.JsonUtils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.Date

internal class RWOneDriveProvider(authoriser: AuthorisationProvider) : ROOneDriveProvider(authoriser) {

    @Throws(IOException::class)
    override fun replaceFile(parent: OneDriveItem, file: File): OneDriveItem {
        if (!parent.isDirectory) {
            throw IllegalArgumentException("Parent is not a folder")
        }

        val request = requestFactory.buildPutRequest(
                OneDriveUrl.putContent(parent.id!!, file.name),
                FileContent(null, file))

        val response = request.execute().parseAs(Item::class.java)
        val item = OneDriveItem.FACTORY.create(response)

        // Now update the item
        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        return updateFile(item, Date(attr.creationTime().toMillis()), Date(attr.lastModifiedTime().toMillis()))
    }

    @Throws(IOException::class)
    override fun uploadFile(parent: OneDriveItem, file: File): OneDriveItem {
        if (!parent.isDirectory) {
            throw IllegalArgumentException("Parent is not a folder")
        }

        // Generate the update item
        val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        val fsi = FileSystemInfoFacet()
        fsi.lastModifiedDateTime = JsonDateSerializer.INSTANCE.serialize(Date(attr.lastModifiedTime().toMillis()))
        fsi.createdDateTime = JsonDateSerializer.INSTANCE.serialize(Date(attr.creationTime().toMillis()))
        val itemToWrite = WriteItemFacet(file.name, fsi, true, false)

        val content = MultipartContent()
                .addPart(Part(
                        HttpHeaders()
                                .set("Content-ID", "<metadata>")
                                .setAcceptEncoding(null),
                        JsonHttpContent(JsonUtils.JSON_FACTORY, itemToWrite)))
                .addPart(Part(
                        HttpHeaders()
                                .set("Content-ID", "<content>")
                                .setAcceptEncoding(null),
                        FileContent(null, file)))

        val request = requestFactory.buildPostRequest(
                OneDriveUrl.postMultiPart(parent.id!!), content)

        request.isLoggingEnabled = true

        return OneDriveItem.FACTORY.create(request.execute().parseAs(Item::class.java))
    }

    @Throws(IOException::class)
    override fun startUploadSession(parent: OneDriveItem, file: File): OneDriveUploadSession {
        val request = requestFactory.buildPostRequest(
                OneDriveUrl.createUploadSession(parent.id!!, file.name),
                JsonHttpContent(JsonUtils.JSON_FACTORY, UploadSessionFacet(file.name)))

        val session = request.execute().parseAs(UploadSession::class.java)

        return OneDriveUploadSession(parent, file, session.uploadUrl!!, session.nextExpectedRanges!!)
    }

    @Throws(IOException::class)
    override fun uploadChunk(session: OneDriveUploadSession) {
        val bytesToUpload = session.chunk
        var item: OneDriveItem

        val request = requestFactory.buildPutRequest(
                GenericUrl(session.uploadUrl),
                ByteArrayContent(null, bytesToUpload))

        request.headers.contentRange = String.format("bytes %d-%d/%d", session.totalUploaded, session.totalUploaded + bytesToUpload.size.toLong() - 1L, session.file.length())

        if (session.totalUploaded + bytesToUpload.size.toLong() < session.file.length()) {
            val response = request.execute().parseAs(UploadSession::class.java)
            session.setRanges(response.nextExpectedRanges!!)
            return
        } else {
            item = OneDriveItem.FACTORY.create(request.execute().parseAs(Item::class.java))
        }

        // If this is the final chunk then set the properties
        val attr = Files.readAttributes(session.file.toPath(), BasicFileAttributes::class.java)
        item = updateFile(item, Date(attr.creationTime().toMillis()), Date(attr.lastModifiedTime().toMillis()))

        // Upload session is now complete
        session.setComplete(item)
    }

    @Throws(IOException::class)
    override fun updateFile(item: OneDriveItem, createdDate: Date, modifiedDate: Date): OneDriveItem {
        val fileSystem = FileSystemInfoFacet()
        fileSystem.createdDateTime = JsonDateSerializer.INSTANCE.serialize(createdDate)
        fileSystem.lastModifiedDateTime = JsonDateSerializer.INSTANCE.serialize(modifiedDate)

        val updateItem = WriteItemFacet(item.name, fileSystem, false, item.isDirectory)

        val request = requestFactory.buildPatchRequest(
                OneDriveUrl.item(item.id!!),
                JsonHttpContent(JsonUtils.JSON_FACTORY, updateItem))

        val response = request.execute().parseAs(Item::class.java)
        return OneDriveItem.FACTORY.create(response)
    }

    @Throws(IOException::class)
    override fun createFolder(parent: OneDriveItem, name: String): OneDriveItem {
        val newFolder = WriteFolderFacet(name)

        val request = requestFactory.buildPostRequest(
                OneDriveUrl.children(parent.id!!),
                JsonHttpContent(JsonUtils.JSON_FACTORY, newFolder))

        val response = request.execute().parseAs(Item::class.java)
        var item = OneDriveItem.FACTORY.create(response)

        // Set the remote timestamps
        if (Paths.get(name).toFile().exists()) {
            val attr = Files.readAttributes(Paths.get(name), BasicFileAttributes::class.java)
            item = updateFile(item, Date(attr.creationTime().toMillis()), Date(attr.lastModifiedTime().toMillis()))
        }

        return item
    }

    @Throws(IOException::class)
    override fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener, chunkSize: Int) {
        var fos: FileOutputStream? = null

        try {
            fos = FileOutputStream(target)
            val downloader = ResumableDownloader(HTTP_TRANSPORT, requestFactory.initializer)
            downloader.progressListener = progressListener

            downloader.chunkSize = chunkSize

            if (chunkSize.toLong() < item.size) {
                // We need to fix the first byte, ranged OneDrive API is bugged
                if (target.path.endsWith(".pdf.tmp"))
                    IOUtils.copy(ByteArrayInputStream("%".toByteArray(StandardCharsets.US_ASCII)), fos)
            }

            downloader.download(OneDriveUrl.content(item.id!!), fos)
        } catch (e: IOException) {
            throw OneDriveAPIException(0, "Unable to download file", e)
        } finally {
            fos?.close()
        }
    }

    @Throws(IOException::class)
    override fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener) {
        download(item, target, progressListener, ResumableDownloader.MAXIMUM_CHUNK_SIZE)
    }

    @Throws(IOException::class)
    override fun delete(remoteFile: OneDriveItem) {
        val request = requestFactory.buildDeleteRequest(OneDriveUrl.item(remoteFile.id!!))
        request.execute()
    }

    internal class WriteFolderFacet(@field:Key
                                    val name: String) {
        @Key
        val folder: FolderFacet

        init {
            this.folder = FolderFacet()
        }
    }

    internal class WriteItemFacet(@field:Key
                                  private val name: String, @field:Key
                                  private val fileSystemInfo: FileSystemInfoFacet, multipart: Boolean, isDirectory: Boolean) {
        @Key
        private var folder: FolderFacet? = null
        @Key
        private var file: FileFacet? = null
        @Key("@content.sourceUrl")
        private val multipart: String?

        init {
            this.multipart = if (multipart) "cid:content" else null

            if (isDirectory) {
                this.folder = FolderFacet()
            } else {
                this.file = FileFacet()
            }
        }
    }

    internal class UploadSessionFacet constructor(name: String) {

        @Key
        val item: FileDetail

        init {
            this.item = FileDetail(name)
        }

        class FileDetail(@field:Key
                         val name: String) {

            @Key("@name.conflictBehavior")
            private val conflictBehavior = "replace"
        }
    }
}
