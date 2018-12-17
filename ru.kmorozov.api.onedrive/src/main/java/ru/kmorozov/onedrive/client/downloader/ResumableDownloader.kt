/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.kmorozov.onedrive.client.downloader

import com.google.api.client.http.*
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.IOUtils
import com.google.api.client.util.Preconditions
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.io.OutputStream

/**
 * Media HTTP Downloader, with support for both direct and resumable media downloads. Documentation
 * is available [here](http://code.google.com/p/google-api-java-client/wiki/MediaDownload).
 *
 *
 *
 *
 * Implementation is not thread-safe.
 *
 *
 *
 *
 *
 * Back-off is disabled by default. To enable it for an abnormal HTTP response and an I/O exception
 * you should call [HttpRequest.setUnsuccessfulResponseHandler] with a new
 * [HttpBackOffUnsuccessfulResponseHandler] instance and
 * [HttpRequest.setIOExceptionHandler] with [HttpBackOffIOExceptionHandler].
 *
 *
 *
 *
 *
 * Upgrade warning: in prior version 1.14 exponential back-off was enabled by default for an
 * abnormal HTTP response. Starting with version 1.15 it's disabled by default.
 *
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @since 1.9
 */
class ResumableDownloader
/**
 * Construct the [ResumableDownloader].
 *
 * @param transport              The transport to use for requests
 * @param httpRequestInitializer The initializer to use when creating an [HttpRequest] or
 * `null` for none
 */
(
        transport: HttpTransport, httpRequestInitializer: HttpRequestInitializer?) {
    /**
     * The request factory for connections to the server.
     */
    private val requestFactory: HttpRequestFactory
    /**
     * The transport to use for requests.
     */
    /**
     * Returns the transport to use for requests.
     */
    val transport: HttpTransport
    /**
     * Determines whether direct media download is enabled or disabled. If value is set to
     * `true` then a direct download will be done where the whole media content is downloaded in
     * a single request. If value is set to `false` then the download uses the resumable media
     * download protocol to download in data chunks. Defaults to `false`.
     */
    private var directDownloadEnabled: Boolean = false
    /**
     * Progress listener to send progress notifications to or `null` for none.
     */
    internal var progressListener: ResumableDownloaderProgressListener? = null
    /**
     * Maximum size of individual chunks that will get downloaded by single HTTP requests. The default
     * value is [.MAXIMUM_CHUNK_SIZE].
     */
    internal var chunkSize = MAXIMUM_CHUNK_SIZE
    /**
     * The length of the HTTP media content or `0` before it is initialized in
     * [.setMediaContentLength].
     */
    private var mediaContentLength: Long = 0
    /**
     * The current state of the downloader.
     */
    /**
     * Gets the current download state of the downloader.
     *
     * @return the download state
     */
    var downloadState = DownloadState.NOT_STARTED
        private set
    /**
     * The total number of bytes downloaded by this downloader.
     */
    /**
     * Gets the total number of bytes downloaded by this downloader.
     *
     * @return the number of bytes downloaded
     */
    var numBytesDownloaded = 1L
        private set
    /**
     * The last byte position of the media file we want to download, default value is `-1`.
     *
     *
     *
     *
     * If its value is `-1` it means there is no upper limit on the byte position.
     *
     */
    /**
     * Gets the last byte position of the media file we want to download or `-1` if there is no
     * upper limit on the byte position.
     *
     * @return the last byte position
     * @since 1.13
     */
    var lastBytePosition = -1L
        private set

    private var totalSize: Long = 0

    /**
     * Gets the download progress denoting the percentage of bytes that have been downloaded,
     * represented between 0.0 (0%) and 1.0 (100%).
     *
     * @return the download progress
     */
    val progress: Double
        get() = if (0L == mediaContentLength) 0.toDouble() else numBytesDownloaded.toDouble() / mediaContentLength.toDouble()

    init {
        this.transport = Preconditions.checkNotNull(transport)
        this.requestFactory = if (null == httpRequestInitializer)
            transport.createRequestFactory()
        else
            transport.createRequestFactory(httpRequestInitializer)
    }

    /**
     * Executes a direct media download or a resumable media download.
     *
     *
     *
     *
     * This method does not close the given output stream.
     *
     *
     *
     *
     *
     * This method is not reentrant. A new instance of [ResumableDownloader] must be
     * instantiated before download called be called again.
     *
     *
     * @param requestUrl   The request URL where the download requests will be sent
     * @param outputStream destination output stream
     */
    @Throws(IOException::class)
    fun download(requestUrl: GenericUrl, outputStream: OutputStream) {
        download(requestUrl, null, outputStream)
    }

    /**
     * Executes a direct media download or a resumable media download.
     *
     *
     *
     *
     * This method does not close the given output stream.
     *
     *
     *
     *
     *
     * This method is not reentrant. A new instance of [ResumableDownloader] must be
     * instantiated before download called be called again.
     *
     *
     * @param requestUrl     request URL where the download requests will be sent
     * @param requestHeaders request headers or `null` to ignore
     * @param outputStream   destination output stream
     * @since 1.12
     */
    @Throws(IOException::class)
    fun download(requestUrl: GenericUrl, requestHeaders: Map<String, *>?, outputStream: OutputStream) {
        Preconditions.checkArgument(DownloadState.NOT_STARTED == downloadState)
        requestUrl["alt"] = "media"

        if (directDownloadEnabled) {
            updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS)
            val response = executeCurrentRequest(lastBytePosition, requestUrl, requestHeaders, outputStream)
            // All required bytes have been downloaded from the server.
            mediaContentLength = response.headers.contentLength!!
            numBytesDownloaded = mediaContentLength
            updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE)
            return
        }

        // Download the media content in chunks.
        while (true) {
            var currentRequestLastBytePos = numBytesDownloaded + chunkSize.toLong() - 1L
            if (-1L != lastBytePosition) {
                // If last byte position has been specified use it iff it is smaller than the chunksize.
                currentRequestLastBytePos = Math.min(lastBytePosition, currentRequestLastBytePos)
            }
            var response: HttpResponse

            try {
                response = executeCurrentRequest(
                        currentRequestLastBytePos, requestUrl, requestHeaders, outputStream)
            } catch (ex: Exception) {
                log.error("Retry because of error: " + ex.message)
                response = executeCurrentRequest(
                        currentRequestLastBytePos, requestUrl, requestHeaders, outputStream)
            }

            val contentRange = response.headers.contentRange

            val nextByteIndex = getNextByteIndex(contentRange)
            setMediaContentLength(contentRange)
            setTotalSize(contentRange)

            if (mediaContentLength <= nextByteIndex) {
                // All required bytes have been downloaded from the server.
                numBytesDownloaded = mediaContentLength
                updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE)
                return
            }

            numBytesDownloaded = nextByteIndex
            updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS)
        }
    }

    /**
     * Executes the current request.
     *
     * @param currentRequestLastBytePos last byte position for current request
     * @param requestUrl                request URL where the download requests will be sent
     * @param requestHeaders            request headers or `null` to ignore
     * @param outputStream              destination output stream
     * @return HTTP response
     */
    @Throws(IOException::class)
    private fun executeCurrentRequest(currentRequestLastBytePos: Long, requestUrl: GenericUrl,
                                      requestHeaders: Map<String, *>?, outputStream: OutputStream): HttpResponse {
        // prepare the GET request
        val request = requestFactory.buildGetRequest(requestUrl)
        // add request headers
        if (null != requestHeaders) {
            request.headers.putAll(requestHeaders)
        }
        // set Range header (if necessary)

        val chunked = false
        if (0L != numBytesDownloaded || -1L != currentRequestLastBytePos) {
            val rangeHeader = StringBuilder()
            rangeHeader.append("bytes=")
            if (0L == totalSize || totalSize - currentRequestLastBytePos > chunkSize.toLong())
                rangeHeader.append(numBytesDownloaded).append('-').append(currentRequestLastBytePos)
            else
                rangeHeader.append('-').append(totalSize - numBytesDownloaded)

            request.headers.range = rangeHeader.toString()

        }

        request.retryOnExecuteIOException = true
        request.ioExceptionHandler = ioExceptionHandler

        // execute the request and copy into the output stream
        val response = request.execute()
        try {
            IOUtils.copy(response.content, outputStream)
        } finally {
            response.disconnect()
        }
        return response
    }

    private fun setTotalSize(rangeHeader: String) {
        if (0L < totalSize)
            return

        totalSize = java.lang.Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1))
    }

    /**
     * Sets the total number of bytes that have been downloaded of the media resource.
     *
     *
     *
     *
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     *
     *
     *
     *
     *
     * Use [.setContentRange] if you need to specify both the bytes downloaded and the last byte
     * position.
     *
     *
     * @param bytesDownloaded The total number of bytes downloaded
     */
    fun setBytesDownloaded(bytesDownloaded: Long): ResumableDownloader {
        Preconditions.checkArgument(0L <= bytesDownloaded)
        this.numBytesDownloaded = bytesDownloaded
        return this
    }

    /**
     * Sets the content range of the next download request. Eg: bytes=firstBytePos-lastBytePos.
     *
     *
     *
     *
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     *
     *
     *
     *
     *
     * Use [.setBytesDownloaded] if you only need to specify the first byte position.
     *
     *
     * @param firstBytePos The first byte position in the content range string
     * @param lastBytePos  The last byte position in the content range string.
     * @since 1.13
     */
    fun setContentRange(firstBytePos: Long, lastBytePos: Int): ResumableDownloader {
        Preconditions.checkArgument(lastBytePos.toLong() >= firstBytePos)
        setBytesDownloaded(firstBytePos)
        this.lastBytePosition = lastBytePos.toLong()
        return this
    }

    /**
     * Sets the media content length from the HTTP Content-Range header (E.g a header of
     * "Content-Range: 0-55/1000" would cause 1000 to be set. `null` headers do not set
     * anything.
     *
     * @param rangeHeader in the HTTP response
     */
    private fun setMediaContentLength(rangeHeader: String?) {
        if (null == rangeHeader) {
            return
        }
        if (0L == mediaContentLength) {
            mediaContentLength = java.lang.Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1))
        }
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to `true`
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to `false` then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to `false`.
     */
    fun isDirectDownloadEnabled(): Boolean {
        return directDownloadEnabled
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to `true`
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to `false` then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to `false`.
     */
    fun setDirectDownloadEnabled(directDownloadEnabled: Boolean): ResumableDownloader {
        this.directDownloadEnabled = directDownloadEnabled
        return this
    }

    /**
     * Returns the progress listener to send progress notifications to or `null` for none.
     */
    fun getProgressListener(): ResumableDownloaderProgressListener? {
        return progressListener
    }

    /**
     * Sets the progress listener to send progress notifications to or `null` for none.
     */
    fun setProgressListener(
            progressListener: ResumableDownloaderProgressListener): ResumableDownloader {
        this.progressListener = progressListener
        return this
    }

    /**
     * Returns the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is [.MAXIMUM_CHUNK_SIZE].
     */
    fun getChunkSize(): Int {
        return chunkSize
    }

    /**
     * Sets the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is [.MAXIMUM_CHUNK_SIZE].
     *
     *
     *
     *
     * The maximum allowable value is [.MAXIMUM_CHUNK_SIZE].
     *
     */
    fun setChunkSize(chunkSize: Int): ResumableDownloader {
        Preconditions.checkArgument(0 < chunkSize && MAXIMUM_CHUNK_SIZE >= chunkSize)
        this.chunkSize = chunkSize
        return this
    }

    /**
     * Sets the download state and notifies the progress listener.
     *
     * @param downloadState value to set to
     */
    @Throws(IOException::class)
    private fun updateStateAndNotifyListener(downloadState: DownloadState) {
        this.downloadState = downloadState
        if (null != progressListener) {
            progressListener!!.progressChanged(this)
        }
    }

    /**
     * Download state associated with the Media HTTP downloader.
     */
    enum class DownloadState {
        /**
         * The download process has not started yet.
         */
        NOT_STARTED,

        /**
         * Set after a media file chunk is downloaded.
         */
        MEDIA_IN_PROGRESS,

        /**
         * Set after the complete media file is successfully downloaded.
         */
        MEDIA_COMPLETE
    }

    companion object {

        private val log = LogManager.getLogger(ResumableDownloader::class.java.name)

        /**
         * Default maximum number of bytes that will be downloaded from the server in any single HTTP
         * request. Set to 32MB because that is the maximum App Engine request size.
         */
        const val MAXIMUM_CHUNK_SIZE = 32 * 0x100000

        /**
         * IOExceptionHandler
         */
        val ioExceptionHandler: HttpIOExceptionHandler = HttpBackOffIOExceptionHandler(ExponentialBackOff())

        /**
         * Returns the next byte index identifying data that the server has not yet sent out, obtained
         * from the HTTP Content-Range header (E.g a header of "Content-Range: 0-55/1000" would cause 56
         * to be returned). `null` headers cause 0 to be returned.
         *
         * @param rangeHeader in the HTTP response
         * @return the byte index beginning where the server has yet to send out data
         */
        private fun getNextByteIndex(rangeHeader: String?): Long {
            return if (null == rangeHeader) {
                0L
            } else java.lang.Long.parseLong(
                    rangeHeader.substring(rangeHeader.indexOf('-') + 1, rangeHeader.indexOf('/'))) + 1L
        }
    }
}