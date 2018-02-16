package com.wouterbreukink.onedrive.client;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.api.client.util.Lists;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import com.wouterbreukink.onedrive.client.downloader.ResumableDownloader;
import com.wouterbreukink.onedrive.client.downloader.ResumableDownloaderProgressListener;
import com.wouterbreukink.onedrive.client.resources.Drive;
import com.wouterbreukink.onedrive.client.resources.Item;
import com.wouterbreukink.onedrive.client.resources.ItemSet;
import com.wouterbreukink.onedrive.client.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.List;

class ROOneDriveProvider implements OneDriveProvider {

    static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();

    private static final String[] EMPTY_STR_ARR = new String[0];

    final HttpRequestFactory requestFactory;

    ROOneDriveProvider(final AuthorisationProvider authoriser) {
        requestFactory =
                HTTP_TRANSPORT.createRequestFactory(request -> {
                    request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));
                    request.setReadTimeout(60000);
                    request.setConnectTimeout(60000);
                    request.setNumberOfRetries(5);
                    try {
                        request.getHeaders().setAuthorization("bearer " + authoriser.getAccessToken());
                    } catch (final IOException e) {
                        throw Throwables.propagate(e);
                    }

                    request.setUnsuccessfulResponseHandler(new OneDriveResponseHandler(authoriser));
                });
    }

    @Override
    public Drive getDefaultDrive() throws IOException {
        final HttpRequest request = requestFactory.buildGetRequest(OneDriveUrl.defaultDrive());
        return request.execute().parseAs(Drive.class);
    }

    @Override
    public OneDriveItem getRoot() throws IOException {
        final HttpRequest request = requestFactory.buildGetRequest(OneDriveUrl.driveRoot());
        final Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    @Override
    public OneDriveItem[] getChildren(final OneDriveItem parent) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Specified Item is not a folder");
        }

        final List<OneDriveItem> itemsToReturn = Lists.newArrayList();

        String token = null;

        do {

            final OneDriveUrl url = OneDriveUrl.children(parent.getId());

            if (null != token) {
                url.setToken(token);
            }

            final HttpRequest request = requestFactory.buildGetRequest(url);
            final HttpResponse response;

            try {
                response = request.execute();
            } catch (SocketException se) {
                return new OneDriveItem[]{};
            }

            final ItemSet items = response.parseAs(ItemSet.class);

            for (final Item i : items.getValue()) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i));
            }

            token = items.getNextToken();

        } while (null != token); // If we have a token for the next page we need to keep going

        return itemsToReturn.toArray(new OneDriveItem[itemsToReturn.size()]);
    }

    @Override
    public OneDriveItem[] getChildren(final String id) throws IOException {
        final List<OneDriveItem> itemsToReturn = Lists.newArrayList();

        String token = null;

        do {

            final OneDriveUrl url = OneDriveUrl.children(id);

            if (null != token) {
                url.setToken(token);
            }

            final HttpRequest request = requestFactory.buildGetRequest(url);
            final ItemSet items = request.execute().parseAs(ItemSet.class);

            for (final Item i : items.getValue()) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i));
            }

            token = items.getNextToken();

        } while (null != token); // If we have a token for the next page we need to keep going

        return itemsToReturn.toArray(new OneDriveItem[itemsToReturn.size()]);
    }

    @Override
    public OneDriveItem getPath(final String path) throws IOException {
        try {
            final HttpRequest request = requestFactory.buildGetRequest(OneDriveUrl.getPath(path));
            final Item response = request.execute().parseAs(Item.class);
            return OneDriveItem.FACTORY.create(response);
        } catch (final HttpResponseException e) {
            throw new OneDriveAPIException(e.getStatusCode(), "Unable to get path", e);
        } catch (final IOException e) {
            throw new OneDriveAPIException(0, "Unable to get path", e);
        }
    }

    @Override
    public OneDriveItem getItem(final String id) throws IOException {
        final HttpRequest request = requestFactory.buildGetRequest(OneDriveUrl.item(id));
        request.setRetryOnExecuteIOException(true);
        request.setIOExceptionHandler(ResumableDownloader.ioExceptionHandler);
        final Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    public OneDriveItem replaceFile(final OneDriveItem parent, final File file) throws IOException {

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        return OneDriveItem.FACTORY.create(parent, file.getName(), file.isDirectory());
    }

    public OneDriveItem uploadFile(final OneDriveItem parent, final File file) throws IOException {

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        return OneDriveItem.FACTORY.create(parent, file.getName(), file.isDirectory());
    }

    @Override
    public OneDriveUploadSession startUploadSession(final OneDriveItem parent, final File file) throws IOException {
        return new OneDriveUploadSession(parent, file, null, EMPTY_STR_ARR);
    }

    @Override
    public void uploadChunk(final OneDriveUploadSession session) throws IOException {
        session.setComplete(OneDriveItem.FACTORY.create(session.getParent(), session.getFile().getName(), session.getFile().isDirectory()));
    }

    @Override
    public OneDriveItem updateFile(final OneDriveItem item, final Date createdDate, final Date modifiedDate) throws IOException {
        // Do nothing, just return the unmodified item
        return item;
    }

    @Override
    public OneDriveItem createFolder(final OneDriveItem parent, final File target) throws IOException {
        // Return a dummy folder
        return OneDriveItem.FACTORY.create(parent, target.getName(), true);
    }

    @Override
    public void download(final OneDriveItem item, final File target, final ResumableDownloaderProgressListener progressListener, final int chunkSize) throws IOException {
        // Do nothing
    }

    @Override
    public void download(final OneDriveItem item, final File target, final ResumableDownloaderProgressListener progressListener) throws IOException {
        // Do nothing
    }

    @Override
    public void delete(final OneDriveItem remoteFile) throws IOException {
        // Do nothing
    }
}
