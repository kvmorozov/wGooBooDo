package ru.kmorozov.onedrive.client

import ru.kmorozov.onedrive.client.resources.Drive
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener

import java.io.File
import java.io.IOException
import java.util.Date

interface OneDriveProvider {

    // Read only operations

    val defaultDrive: Drive

    val root: OneDriveItem?

    @Throws(IOException::class)
    fun getChildren(parent: OneDriveItem): Array<OneDriveItem>

    @Throws(IOException::class)
    fun getChildren(id: String): Array<OneDriveItem>

    @Throws(IOException::class)
    fun getPath(path: String): OneDriveItem

    @Throws(IOException::class)
    fun getItem(id: String): OneDriveItem

    // Write operations

    @Throws(IOException::class)
    fun replaceFile(parent: OneDriveItem, file: File): OneDriveItem

    @Throws(IOException::class)
    fun uploadFile(parent: OneDriveItem, file: File): OneDriveItem

    @Throws(IOException::class)
    fun startUploadSession(parent: OneDriveItem, file: File): OneDriveUploadSession

    @Throws(IOException::class)
    fun uploadChunk(session: OneDriveUploadSession)

    @Throws(IOException::class)
    fun updateFile(item: OneDriveItem, createdDate: Date, modifiedDate: Date): OneDriveItem

    @Throws(IOException::class)
    fun createFolder(parent: OneDriveItem, name: String): OneDriveItem

    @Throws(IOException::class)
    fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener, chunkSize: Int)

    @Throws(IOException::class)
    fun download(item: OneDriveItem, target: File, progressListener: ResumableDownloaderProgressListener)

    @Throws(IOException::class)
    fun delete(remoteFile: OneDriveItem)

    @Throws(IOException::class)
    fun search(query: String): Array<OneDriveItem>

    object FACTORY {

        fun readOnlyApi(authoriser: AuthorisationProvider): OneDriveProvider {
            return ROOneDriveProvider(authoriser)
        }

        fun readWriteApi(authoriser: AuthorisationProvider): OneDriveProvider {
            return RWOneDriveProvider(authoriser)
        }
    }
}
