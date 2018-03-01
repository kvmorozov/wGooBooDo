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

package ru.kmorozov.onedrive.client.downloader;

import com.google.api.client.http.*;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Media HTTP Downloader, with support for both direct and resumable media downloads. Documentation
 * is available <a
 * href='http://code.google.com/p/google-api-java-client/wiki/MediaDownload'>here</a>.
 * <p>
 * <p>
 * Implementation is not thread-safe.
 * </p>
 * <p>
 * <p>
 * Back-off is disabled by default. To enable it for an abnormal HTTP response and an I/O exception
 * you should call {@link HttpRequest#setUnsuccessfulResponseHandler} with a new
 * {@link HttpBackOffUnsuccessfulResponseHandler} instance and
 * {@link HttpRequest#setIOExceptionHandler} with {@link HttpBackOffIOExceptionHandler}.
 * </p>
 * <p>
 * <p>
 * Upgrade warning: in prior version 1.14 exponential back-off was enabled by default for an
 * abnormal HTTP response. Starting with version 1.15 it's disabled by default.
 * </p>
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @since 1.9
 */
public final class ResumableDownloader {

    private static final Logger log = LogManager.getLogger(ResumableDownloader.class.getName());

    /**
     * Default maximum number of bytes that will be downloaded from the server in any single HTTP
     * request. Set to 32MB because that is the maximum App Engine request size.
     */
    public static final int MAXIMUM_CHUNK_SIZE = 32 * 0x100000;
    /**
     * The request factory for connections to the server.
     */
    private final HttpRequestFactory requestFactory;
    /**
     * The transport to use for requests.
     */
    private final HttpTransport transport;
    /**
     * Determines whether direct media download is enabled or disabled. If value is set to
     * {@code true} then a direct download will be done where the whole media content is downloaded in
     * a single request. If value is set to {@code false} then the download uses the resumable media
     * download protocol to download in data chunks. Defaults to {@code false}.
     */
    private boolean directDownloadEnabled;
    /**
     * Progress listener to send progress notifications to or {@code null} for none.
     */
    private ResumableDownloaderProgressListener progressListener;
    /**
     * Maximum size of individual chunks that will get downloaded by single HTTP requests. The default
     * value is {@link #MAXIMUM_CHUNK_SIZE}.
     */
    private int chunkSize = MAXIMUM_CHUNK_SIZE;
    /**
     * The length of the HTTP media content or {@code 0} before it is initialized in
     * {@link #setMediaContentLength}.
     */
    private long mediaContentLength;
    /**
     * The current state of the downloader.
     */
    private DownloadState downloadState = DownloadState.NOT_STARTED;
    /**
     * The total number of bytes downloaded by this downloader.
     */
    private long bytesDownloaded = 1;
    /**
     * The last byte position of the media file we want to download, default value is {@code -1}.
     * <p>
     * <p>
     * If its value is {@code -1} it means there is no upper limit on the byte position.
     * </p>
     */
    private long lastBytePos = -1;

    private long totalSize;

    /**
     * IOExceptionHandler
     */
    public static final HttpIOExceptionHandler ioExceptionHandler = new HttpBackOffIOExceptionHandler(new ExponentialBackOff());

    /**
     * Construct the {@link ResumableDownloader}.
     *
     * @param transport              The transport to use for requests
     * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
     *                               {@code null} for none
     */
    public ResumableDownloader(
            final HttpTransport transport, final HttpRequestInitializer httpRequestInitializer) {
        this.transport = Preconditions.checkNotNull(transport);
        this.requestFactory = null == httpRequestInitializer
                ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
    }

    /**
     * Executes a direct media download or a resumable media download.
     * <p>
     * <p>
     * This method does not close the given output stream.
     * </p>
     * <p>
     * <p>
     * This method is not reentrant. A new instance of {@link ResumableDownloader} must be
     * instantiated before download called be called again.
     * </p>
     *
     * @param requestUrl   The request URL where the download requests will be sent
     * @param outputStream destination output stream
     */
    public void download(final GenericUrl requestUrl, final OutputStream outputStream) throws IOException {
        download(requestUrl, null, outputStream);
    }

    /**
     * Executes a direct media download or a resumable media download.
     * <p>
     * <p>
     * This method does not close the given output stream.
     * </p>
     * <p>
     * <p>
     * This method is not reentrant. A new instance of {@link ResumableDownloader} must be
     * instantiated before download called be called again.
     * </p>
     *
     * @param requestUrl     request URL where the download requests will be sent
     * @param requestHeaders request headers or {@code null} to ignore
     * @param outputStream   destination output stream
     * @since 1.12
     */
    public void download(final GenericUrl requestUrl, final Map requestHeaders, final OutputStream outputStream)
            throws IOException {
        Preconditions.checkArgument(DownloadState.NOT_STARTED == downloadState);
        requestUrl.put("alt", "media");

        if (directDownloadEnabled) {
            updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS);
            final HttpResponse response =
                    executeCurrentRequest(lastBytePos, requestUrl, requestHeaders, outputStream);
            // All required bytes have been downloaded from the server.
            mediaContentLength = response.getHeaders().getContentLength();
            bytesDownloaded = mediaContentLength;
            updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE);
            return;
        }

        // Download the media content in chunks.
        while (true) {
            long currentRequestLastBytePos = bytesDownloaded + chunkSize - 1;
            if (-1 != lastBytePos) {
                // If last byte position has been specified use it iff it is smaller than the chunksize.
                currentRequestLastBytePos = Math.min(lastBytePos, currentRequestLastBytePos);
            }
            HttpResponse response = null;

            try {
                response = executeCurrentRequest(
                        currentRequestLastBytePos, requestUrl, requestHeaders, outputStream);
            } catch (final Exception ex) {
                log.error("Retry because of error: " + ex.getMessage());
                response = executeCurrentRequest(
                        currentRequestLastBytePos, requestUrl, requestHeaders, outputStream);
            }

            final String contentRange = response.getHeaders().getContentRange();

            final long nextByteIndex = getNextByteIndex(contentRange);
            setMediaContentLength(contentRange);
            setTotalSize(contentRange);

            if (mediaContentLength <= nextByteIndex) {
                // All required bytes have been downloaded from the server.
                bytesDownloaded = mediaContentLength;
                updateStateAndNotifyListener(DownloadState.MEDIA_COMPLETE);
                return;
            }

            bytesDownloaded = nextByteIndex;
            updateStateAndNotifyListener(DownloadState.MEDIA_IN_PROGRESS);
        }
    }

    /**
     * Executes the current request.
     *
     * @param currentRequestLastBytePos last byte position for current request
     * @param requestUrl                request URL where the download requests will be sent
     * @param requestHeaders            request headers or {@code null} to ignore
     * @param outputStream              destination output stream
     * @return HTTP response
     */
    private HttpResponse executeCurrentRequest(final long currentRequestLastBytePos, final GenericUrl requestUrl,
                                               final Map requestHeaders, final OutputStream outputStream) throws IOException {
        // prepare the GET request
        final HttpRequest request = requestFactory.buildGetRequest(requestUrl);
        // add request headers
        if (null != requestHeaders) {
            request.getHeaders().putAll(requestHeaders);
        }
        // set Range header (if necessary)

        boolean chunked = false;
        if (0 != bytesDownloaded || -1 != currentRequestLastBytePos) {
            final StringBuilder rangeHeader = new StringBuilder();
            rangeHeader.append("bytes=");
            if (0 == totalSize || totalSize - currentRequestLastBytePos > chunkSize)
                rangeHeader.append(bytesDownloaded).append('-').append(currentRequestLastBytePos);
            else
                rangeHeader.append('-').append(totalSize - bytesDownloaded);

            request.getHeaders().setRange(rangeHeader.toString());

            chunked = true;
        }

        request.setRetryOnExecuteIOException(true);
        request.setIOExceptionHandler(ioExceptionHandler);

        // execute the request and copy into the output stream
        final HttpResponse response = request.execute();
        try {
            IOUtils.copy(response.getContent(), outputStream);
        } finally

        {
            response.disconnect();
        }
        return response;
    }

    /**
     * Returns the next byte index identifying data that the server has not yet sent out, obtained
     * from the HTTP Content-Range header (E.g a header of "Content-Range: 0-55/1000" would cause 56
     * to be returned). <code>null</code> headers cause 0 to be returned.
     *
     * @param rangeHeader in the HTTP response
     * @return the byte index beginning where the server has yet to send out data
     */
    private static long getNextByteIndex(final String rangeHeader) {
        if (null == rangeHeader) {
            return 0L;
        }
        return Long.parseLong(
                rangeHeader.substring(rangeHeader.indexOf('-') + 1, rangeHeader.indexOf('/'))) + 1;
    }

    private void setTotalSize(final String rangeHeader) {
        if (0 < totalSize)
            return;

        totalSize = Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1));
    }

    /**
     * Sets the total number of bytes that have been downloaded of the media resource.
     * <p>
     * <p>
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     * </p>
     * <p>
     * <p>
     * Use {@link #setContentRange} if you need to specify both the bytes downloaded and the last byte
     * position.
     * </p>
     *
     * @param bytesDownloaded The total number of bytes downloaded
     */
    public ResumableDownloader setBytesDownloaded(final long bytesDownloaded) {
        Preconditions.checkArgument(0 <= bytesDownloaded);
        this.bytesDownloaded = bytesDownloaded;
        return this;
    }

    /**
     * Sets the content range of the next download request. Eg: bytes=firstBytePos-lastBytePos.
     * <p>
     * <p>
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     * </p>
     * <p>
     * <p>
     * Use {@link #setBytesDownloaded} if you only need to specify the first byte position.
     * </p>
     *
     * @param firstBytePos The first byte position in the content range string
     * @param lastBytePos  The last byte position in the content range string.
     * @since 1.13
     */
    public ResumableDownloader setContentRange(final long firstBytePos, final int lastBytePos) {
        Preconditions.checkArgument(lastBytePos >= firstBytePos);
        setBytesDownloaded(firstBytePos);
        this.lastBytePos = lastBytePos;
        return this;
    }

    /**
     * Sets the media content length from the HTTP Content-Range header (E.g a header of
     * "Content-Range: 0-55/1000" would cause 1000 to be set. <code>null</code> headers do not set
     * anything.
     *
     * @param rangeHeader in the HTTP response
     */
    private void setMediaContentLength(final String rangeHeader) {
        if (null == rangeHeader) {
            return;
        }
        if (0 == mediaContentLength) {
            mediaContentLength = Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1));
        }
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to {@code false} then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to {@code false}.
     */
    public boolean isDirectDownloadEnabled() {
        return directDownloadEnabled;
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to {@code false} then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to {@code false}.
     */
    public ResumableDownloader setDirectDownloadEnabled(final boolean directDownloadEnabled) {
        this.directDownloadEnabled = directDownloadEnabled;
        return this;
    }

    /**
     * Returns the progress listener to send progress notifications to or {@code null} for none.
     */
    public ResumableDownloaderProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Sets the progress listener to send progress notifications to or {@code null} for none.
     */
    public ResumableDownloader setProgressListener(
            final ResumableDownloaderProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * Returns the transport to use for requests.
     */
    public HttpTransport getTransport() {
        return transport;
    }

    /**
     * Returns the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Sets the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
     * <p>
     * <p>
     * The maximum allowable value is {@link #MAXIMUM_CHUNK_SIZE}.
     * </p>
     */
    public ResumableDownloader setChunkSize(final int chunkSize) {
        Preconditions.checkArgument(0 < chunkSize && MAXIMUM_CHUNK_SIZE >= chunkSize);
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Gets the total number of bytes downloaded by this downloader.
     *
     * @return the number of bytes downloaded
     */
    public long getNumBytesDownloaded() {
        return bytesDownloaded;
    }

    /**
     * Gets the last byte position of the media file we want to download or {@code -1} if there is no
     * upper limit on the byte position.
     *
     * @return the last byte position
     * @since 1.13
     */
    public long getLastBytePosition() {
        return lastBytePos;
    }

    /**
     * Sets the download state and notifies the progress listener.
     *
     * @param downloadState value to set to
     */
    private void updateStateAndNotifyListener(final DownloadState downloadState) throws IOException {
        this.downloadState = downloadState;
        if (null != progressListener) {
            progressListener.progressChanged(this);
        }
    }

    /**
     * Gets the current download state of the downloader.
     *
     * @return the download state
     */
    public DownloadState getDownloadState() {
        return downloadState;
    }

    /**
     * Gets the download progress denoting the percentage of bytes that have been downloaded,
     * represented between 0.0 (0%) and 1.0 (100%).
     *
     * @return the download progress
     */
    public double getProgress() {
        return 0 == mediaContentLength ? 0 : (double) bytesDownloaded / mediaContentLength;
    }

    /**
     * Download state associated with the Media HTTP downloader.
     */
    public enum DownloadState {
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
}