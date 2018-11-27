package ru.kmorozov.onedrive.client;

import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Key;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloader;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener;
import ru.kmorozov.onedrive.client.facets.FileFacet;
import ru.kmorozov.onedrive.client.facets.FileSystemInfoFacet;
import ru.kmorozov.onedrive.client.facets.FolderFacet;
import ru.kmorozov.onedrive.client.resources.Item;
import ru.kmorozov.onedrive.client.resources.UploadSession;
import ru.kmorozov.onedrive.client.serialization.JsonDateSerializer;
import ru.kmorozov.onedrive.client.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

class RWOneDriveProvider extends ROOneDriveProvider {

    RWOneDriveProvider(AuthorisationProvider authoriser) {
        super(authoriser);
    }

    @Override
    public OneDriveItem replaceFile(OneDriveItem parent, File file) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        HttpRequest request = this.requestFactory.buildPutRequest(
                OneDriveUrl.putContent(parent.getId(), file.getName()),
                new FileContent(null, file));

        Item response = request.execute().parseAs(Item.class);
        OneDriveItem item = OneDriveItem.FACTORY.create(response);

        // Now update the item
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return this.updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));
    }

    @Override
    public OneDriveItem uploadFile(OneDriveItem parent, File file) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        // Generate the update item
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileSystemInfoFacet fsi = new FileSystemInfoFacet();
        fsi.setLastModifiedDateTime(JsonDateSerializer.INSTANCE.serialize(new Date(attr.lastModifiedTime().toMillis())));
        fsi.setCreatedDateTime(JsonDateSerializer.INSTANCE.serialize(new Date(attr.creationTime().toMillis())));
        RWOneDriveProvider.WriteItemFacet itemToWrite = new RWOneDriveProvider.WriteItemFacet(file.getName(), fsi, true, false);

        MultipartContent content = new MultipartContent()
                .addPart(new MultipartContent.Part(
                        new HttpHeaders()
                                .set("Content-ID", "<metadata>")
                                .setAcceptEncoding(null),
                        new JsonHttpContent(JsonUtils.JSON_FACTORY, itemToWrite)))
                .addPart(new MultipartContent.Part(
                        new HttpHeaders()
                                .set("Content-ID", "<content>")
                                .setAcceptEncoding(null),
                        new FileContent(null, file)));

        HttpRequest request = this.requestFactory.buildPostRequest(
                OneDriveUrl.postMultiPart(parent.getId()), content);

        request.setLoggingEnabled(true);

        return OneDriveItem.FACTORY.create(request.execute().parseAs(Item.class));
    }

    @Override
    public OneDriveUploadSession startUploadSession(OneDriveItem parent, File file) throws IOException {
        HttpRequest request = this.requestFactory.buildPostRequest(
                OneDriveUrl.createUploadSession(parent.getId(), file.getName()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, new RWOneDriveProvider.UploadSessionFacet(file.getName())));

        UploadSession session = request.execute().parseAs(UploadSession.class);

        return new OneDriveUploadSession(parent, file, session.getUploadUrl(), session.getNextExpectedRanges());
    }

    @Override
    public void uploadChunk(OneDriveUploadSession session) throws IOException {
        byte[] bytesToUpload = session.getChunk();
        OneDriveItem item;

        HttpRequest request = this.requestFactory.buildPutRequest(
                new GenericUrl(session.getUploadUrl()),
                new ByteArrayContent(null, bytesToUpload));

        request.getHeaders().setContentRange(String.format("bytes %d-%d/%d", session.getTotalUploaded(), session.getTotalUploaded() + (long) bytesToUpload.length - 1L, session.getFile().length()));

        if (session.getTotalUploaded() + (long) bytesToUpload.length < session.getFile().length()) {
            UploadSession response = request.execute().parseAs(UploadSession.class);
            session.setRanges(response.getNextExpectedRanges());
            return;
        } else {
            item = OneDriveItem.FACTORY.create(request.execute().parseAs(Item.class));
        }

        // If this is the final chunk then set the properties
        BasicFileAttributes attr = Files.readAttributes(session.getFile().toPath(), BasicFileAttributes.class);
        item = this.updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));

        // Upload session is now complete
        session.setComplete(item);
    }

    @Override
    public OneDriveItem updateFile(OneDriveItem item, Date createdDate, Date modifiedDate) throws IOException {
        FileSystemInfoFacet fileSystem = new FileSystemInfoFacet();
        fileSystem.setCreatedDateTime(JsonDateSerializer.INSTANCE.serialize(createdDate));
        fileSystem.setLastModifiedDateTime(JsonDateSerializer.INSTANCE.serialize(modifiedDate));

        RWOneDriveProvider.WriteItemFacet updateItem = new RWOneDriveProvider.WriteItemFacet(item.getName(), fileSystem, false, item.isDirectory());

        HttpRequest request = this.requestFactory.buildPatchRequest(
                OneDriveUrl.item(item.getId()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, updateItem));

        Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    @Override
    public OneDriveItem createFolder(OneDriveItem parent, String name) throws IOException {
        RWOneDriveProvider.WriteFolderFacet newFolder = new RWOneDriveProvider.WriteFolderFacet(name);

        HttpRequest request = this.requestFactory.buildPostRequest(
                OneDriveUrl.children(parent.getId()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, newFolder));

        Item response = request.execute().parseAs(Item.class);
        OneDriveItem item = OneDriveItem.FACTORY.create(response);

        // Set the remote timestamps
        if (Paths.get(name).toFile().exists()) {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(name), BasicFileAttributes.class);
            item = this.updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));
        }

        return item;
    }

    @Override
    public void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener, int chunkSize) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(target);
            ResumableDownloader downloader = new ResumableDownloader(ROOneDriveProvider.HTTP_TRANSPORT, this.requestFactory.getInitializer());
            downloader.setProgressListener(progressListener);

            downloader.setChunkSize(chunkSize);

            if ((long) chunkSize < item.getSize()) {
                // We need to fix the first byte, ranged OneDrive API is bugged
                if (target.getPath().endsWith(".pdf.tmp"))
                    IOUtils.copy(new ByteArrayInputStream("%".getBytes(StandardCharsets.US_ASCII)), fos);
            }

            downloader.download(OneDriveUrl.content(item.getId()), fos);
        } catch (IOException e) {
            throw new OneDriveAPIException(0, "Unable to download file", e);
        } finally {
            if (null != fos) {
                fos.close();
            }
        }
    }

    @Override
    public void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener) throws IOException {
        this.download(item, target, progressListener, ResumableDownloader.MAXIMUM_CHUNK_SIZE);
    }

    @Override
    public void delete(OneDriveItem remoteFile) throws IOException {
        HttpRequest request = this.requestFactory.buildDeleteRequest(OneDriveUrl.item(remoteFile.getId()));
        request.execute();
    }

    static class WriteFolderFacet {
        @Key
        private final String name;
        @Key
        private final FolderFacet folder;

        WriteFolderFacet(String name) {
            this.name = name;
            folder = new FolderFacet();
        }

        public String getName() {
            return this.name;
        }

        public FolderFacet getFolder() {
            return this.folder;
        }
    }

    static class WriteItemFacet {
        @Key
        private final FileSystemInfoFacet fileSystemInfo;
        @Key
        private final String name;
        @Key
        private FolderFacet folder;
        @Key
        private FileFacet file;
        @Key("@content.sourceUrl")
        private final String multipart;

        WriteItemFacet(String name, FileSystemInfoFacet fileSystemInfo, boolean multipart, boolean isDirectory) {
            this.name = name;
            this.fileSystemInfo = fileSystemInfo;
            this.multipart = multipart ? "cid:content" : null;

            if (isDirectory) {
                folder = new FolderFacet();
            } else {
                file = new FileFacet();
            }
        }
    }

    static final class UploadSessionFacet {

        @Key
        private final RWOneDriveProvider.UploadSessionFacet.FileDetail item;

        private UploadSessionFacet(String name) {
            item = new RWOneDriveProvider.UploadSessionFacet.FileDetail(name);
        }

        public RWOneDriveProvider.UploadSessionFacet.FileDetail getItem() {
            return this.item;
        }

        public static class FileDetail {

            @Key
            private final String name;

            @Key("@name.conflictBehavior")
            private final String conflictBehavior = "replace";

            public FileDetail(String name) {
                this.name = name;
            }

            public String getName() {
                return this.name;
            }
        }
    }
}
