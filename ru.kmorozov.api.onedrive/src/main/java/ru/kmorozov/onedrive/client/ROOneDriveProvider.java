package ru.kmorozov.onedrive.client;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Lists;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener;
import ru.kmorozov.onedrive.client.resources.Drive;
import ru.kmorozov.onedrive.client.resources.Item;
import ru.kmorozov.onedrive.client.resources.ItemSet;
import ru.kmorozov.onedrive.client.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.List;

class ROOneDriveProvider implements OneDriveProvider {

    protected static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();

    private static final String[] EMPTY_STR_ARR = new String[0];

    protected final HttpRequestFactory requestFactory;

    private Drive cachedDefaultDrive;

    ROOneDriveProvider(AuthorisationProvider authoriser) {
        this.requestFactory =
                ROOneDriveProvider.HTTP_TRANSPORT.createRequestFactory(request -> {
                    request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));
                    request.setReadTimeout(60000);
                    request.setConnectTimeout(60000);
                    request.setNumberOfRetries(5);
                    try {
                        request.getHeaders().setAuthorization("bearer " + authoriser.getAccessToken());
                    } catch (IOException e) {
                        throw e;
                    }

                    request.setUnsuccessfulResponseHandler(new OneDriveResponseHandler(authoriser));
                });
    }

    @Override
    public Drive getDefaultDrive() throws IOException {
        if (this.cachedDefaultDrive == null) {
            HttpRequest request = this.requestFactory.buildGetRequest(OneDriveUrl.defaultDrive());
            this.cachedDefaultDrive = request.execute().parseAs(Drive.class);
        }

        return this.cachedDefaultDrive;
    }

    @Override
    public OneDriveItem getRoot() throws IOException {
        HttpRequest request = this.requestFactory.buildGetRequest(OneDriveUrl.driveRoot());
        Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    @Override
    public OneDriveItem[] getChildren(OneDriveItem parent) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Specified Item is not a folder");
        }

        List<OneDriveItem> itemsToReturn = Lists.newArrayList();

        String token = null;

        do {

            OneDriveUrl url = OneDriveUrl.children(parent.getId());

            if (null != token) {
                url.setToken(token);
            }

            HttpRequest request = this.requestFactory.buildGetRequest(url);
            HttpResponse response;

            try {
                response = request.execute();
            } catch (final SocketException se) {
                return new OneDriveItem[]{};
            }

            ItemSet items = response.parseAs(ItemSet.class);

            for (Item i : items.getValue()) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i));
            }

            token = items.getNextToken();

        } while (null != token); // If we have a token for the next page we need to keep going

        return itemsToReturn.toArray(new OneDriveItem[0]);
    }

    @Override
    public OneDriveItem[] getChildren(String id) throws IOException {
        List<OneDriveItem> itemsToReturn = Lists.newArrayList();

        String token = null;

        do {

            OneDriveUrl url = OneDriveUrl.children(id);

            if (null != token) {
                url.setToken(token);
            }

            HttpRequest request = this.requestFactory.buildGetRequest(url);
            ItemSet items = request.execute().parseAs(ItemSet.class);

            for (Item i : items.getValue()) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i));
            }

            token = items.getNextToken();

        } while (null != token); // If we have a token for the next page we need to keep going

        return itemsToReturn.toArray(new OneDriveItem[0]);
    }

    @Override
    public OneDriveItem getPath(String path) throws IOException {
        try {
            HttpRequest request = this.requestFactory.buildGetRequest(OneDriveUrl.getPath(path));
            Item response = request.execute().parseAs(Item.class);
            return OneDriveItem.FACTORY.create(response);
        } catch (HttpResponseException e) {
            throw new OneDriveAPIException(e.getStatusCode(), "Unable to get path", e);
        } catch (IOException e) {
            throw new OneDriveAPIException(0, "Unable to get path", e);
        }
    }

    @Override
    public OneDriveItem getItem(String id) throws IOException {
        HttpRequest request = this.requestFactory.buildGetRequest(OneDriveUrl.item(id));
        request.setRetryOnExecuteIOException(true);
        request.setIOExceptionHandler(ResumableDownloader.ioExceptionHandler);
        Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    public OneDriveItem replaceFile(OneDriveItem parent, File file) throws IOException {

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        return OneDriveItem.FACTORY.create(parent, file.getName(), file.isDirectory());
    }

    public OneDriveItem uploadFile(OneDriveItem parent, File file) throws IOException {

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        return OneDriveItem.FACTORY.create(parent, file.getName(), file.isDirectory());
    }

    @Override
    public OneDriveUploadSession startUploadSession(OneDriveItem parent, File file) throws IOException {
        return new OneDriveUploadSession(parent, file, null, ROOneDriveProvider.EMPTY_STR_ARR);
    }

    @Override
    public void uploadChunk(OneDriveUploadSession session) throws IOException {
        session.setComplete(OneDriveItem.FACTORY.create(session.getParent(), session.getFile().getName(), session.getFile().isDirectory()));
    }

    @Override
    public OneDriveItem updateFile(OneDriveItem item, Date createdDate, Date modifiedDate) throws IOException {
        // Do nothing, just return the unmodified item
        return item;
    }

    @Override
    public OneDriveItem createFolder(OneDriveItem parent, String name) throws IOException {
        // Return a dummy folder
        return OneDriveItem.FACTORY.create(parent, name, true);
    }

    @Override
    public void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener, int chunkSize) throws IOException {
        // Do nothing
    }

    @Override
    public void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener) throws IOException {
        // Do nothing
    }

    @Override
    public void delete(OneDriveItem remoteFile) throws IOException {
        // Do nothing
    }

    @Override
    public OneDriveItem[] search(final String query) throws IOException {
        List<OneDriveItem> itemsToReturn = Lists.newArrayList();

        String token = null;

        do {

            OneDriveUrl url = OneDriveUrl.search(this.getDefaultDrive(), query);

            if (null != token) {
                url.setToken(token);
            }

            HttpRequest request = this.requestFactory.buildGetRequest(url);
            ItemSet items = request.execute().parseAs(ItemSet.class);

            for (Item i : items.getValue()) {
                itemsToReturn.add(OneDriveItem.FACTORY.create(i));
            }

            token = items.getNextToken();

        } while (null != token); // If we have a token for the next page we need to keep going

        return itemsToReturn.toArray(new OneDriveItem[0]);
    }
}
