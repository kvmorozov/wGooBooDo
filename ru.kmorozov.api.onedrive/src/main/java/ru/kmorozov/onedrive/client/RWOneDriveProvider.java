package ru.kmorozov.onedrive.client;

import com.google.api.client.http.*;
import com.google.api.client.http.MultipartContent.Part;
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

    RWOneDriveProvider(final AuthorisationProvider authoriser) {
        super(authoriser);
    }

    @Override
    public OneDriveItem replaceFile(final OneDriveItem parent, final File file) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        final HttpRequest request = requestFactory.buildPutRequest(
                OneDriveUrl.putContent(parent.getId(), file.getName()),
                new FileContent(null, file));

        final Item response = request.execute().parseAs(Item.class);
        final OneDriveItem item = OneDriveItem.FACTORY.create(response);

        // Now update the item
        final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));
    }

    @Override
    public OneDriveItem uploadFile(final OneDriveItem parent, final File file) throws IOException {
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a folder");
        }

        // Generate the update item
        final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        final FileSystemInfoFacet fsi = new FileSystemInfoFacet();
        fsi.setLastModifiedDateTime(JsonDateSerializer.INSTANCE.serialize(new Date(attr.lastModifiedTime().toMillis())));
        fsi.setCreatedDateTime(JsonDateSerializer.INSTANCE.serialize(new Date(attr.creationTime().toMillis())));
        final WriteItemFacet itemToWrite = new WriteItemFacet(file.getName(), fsi, true, false);

        final MultipartContent content = new MultipartContent()
                .addPart(new Part(
                        new HttpHeaders()
                                .set("Content-ID", "<metadata>")
                                .setAcceptEncoding(null),
                        new JsonHttpContent(JsonUtils.JSON_FACTORY, itemToWrite)))
                .addPart(new Part(
                        new HttpHeaders()
                                .set("Content-ID", "<content>")
                                .setAcceptEncoding(null),
                        new FileContent(null, file)));

        final HttpRequest request = requestFactory.buildPostRequest(
                OneDriveUrl.postMultiPart(parent.getId()), content);

        request.setLoggingEnabled(true);

        return OneDriveItem.FACTORY.create(request.execute().parseAs(Item.class));
    }

    @Override
    public OneDriveUploadSession startUploadSession(final OneDriveItem parent, final File file) throws IOException {
        final HttpRequest request = requestFactory.buildPostRequest(
                OneDriveUrl.createUploadSession(parent.getId(), file.getName()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, new UploadSessionFacet(file.getName())));

        final UploadSession session = request.execute().parseAs(UploadSession.class);

        return new OneDriveUploadSession(parent, file, session.getUploadUrl(), session.getNextExpectedRanges());
    }

    @Override
    public void uploadChunk(final OneDriveUploadSession session) throws IOException {
        final byte[] bytesToUpload = session.getChunk();
        OneDriveItem item;

        final HttpRequest request = requestFactory.buildPutRequest(
                new GenericUrl(session.getUploadUrl()),
                new ByteArrayContent(null, bytesToUpload));

        request.getHeaders().setContentRange(String.format("bytes %d-%d/%d", session.getTotalUploaded(), session.getTotalUploaded() + (long) bytesToUpload.length - 1L, session.getFile().length()));

        if (session.getTotalUploaded() + (long) bytesToUpload.length < session.getFile().length()) {
            final UploadSession response = request.execute().parseAs(UploadSession.class);
            session.setRanges(response.getNextExpectedRanges());
            return;
        } else {
            item = OneDriveItem.FACTORY.create(request.execute().parseAs(Item.class));
        }

        // If this is the final chunk then set the properties
        final BasicFileAttributes attr = Files.readAttributes(session.getFile().toPath(), BasicFileAttributes.class);
        item = updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));

        // Upload session is now complete
        session.setComplete(item);
    }

    @Override
    public OneDriveItem updateFile(final OneDriveItem item, final Date createdDate, final Date modifiedDate) throws IOException {
        final FileSystemInfoFacet fileSystem = new FileSystemInfoFacet();
        fileSystem.setCreatedDateTime(JsonDateSerializer.INSTANCE.serialize(createdDate));
        fileSystem.setLastModifiedDateTime(JsonDateSerializer.INSTANCE.serialize(modifiedDate));

        final WriteItemFacet updateItem = new WriteItemFacet(item.getName(), fileSystem, false, item.isDirectory());

        final HttpRequest request = requestFactory.buildPatchRequest(
                OneDriveUrl.item(item.getId()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, updateItem));

        final Item response = request.execute().parseAs(Item.class);
        return OneDriveItem.FACTORY.create(response);
    }

    @Override
    public OneDriveItem createFolder(final OneDriveItem parent, final String name) throws IOException {
        final WriteFolderFacet newFolder = new WriteFolderFacet(name);

        final HttpRequest request = requestFactory.buildPostRequest(
                OneDriveUrl.children(parent.getId()),
                new JsonHttpContent(JsonUtils.JSON_FACTORY, newFolder));

        final Item response = request.execute().parseAs(Item.class);
        OneDriveItem item = OneDriveItem.FACTORY.create(response);

        // Set the remote timestamps
        if (Paths.get(name).toFile().exists()) {
            final BasicFileAttributes attr = Files.readAttributes(Paths.get(name), BasicFileAttributes.class);
            item = updateFile(item, new Date(attr.creationTime().toMillis()), new Date(attr.lastModifiedTime().toMillis()));
        }

        return item;
    }

    @Override
    public void download(final OneDriveItem item, final File target, final ResumableDownloaderProgressListener progressListener, final int chunkSize) throws IOException {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(target);
            final ResumableDownloader downloader = new ResumableDownloader(ROOneDriveProvider.HTTP_TRANSPORT, requestFactory.getInitializer());
            downloader.setProgressListener(progressListener);

            downloader.setChunkSize(chunkSize);

            if ((long) chunkSize < item.getSize()) {
                // We need to fix the first byte, ranged OneDrive API is bugged
                if (target.getPath().endsWith(".pdf.tmp"))
                    IOUtils.copy(new ByteArrayInputStream("%".getBytes(StandardCharsets.US_ASCII)), fos);
            }

            downloader.download(OneDriveUrl.content(item.getId()), fos);
        } catch (final IOException e) {
            throw new OneDriveAPIException(0, "Unable to download file", e);
        } finally {
            if (null != fos) {
                fos.close();
            }
        }
    }

    @Override
    public void download(final OneDriveItem item, final File target, final ResumableDownloaderProgressListener progressListener) throws IOException {
        download(item, target, progressListener, ResumableDownloader.MAXIMUM_CHUNK_SIZE);
    }

    @Override
    public void delete(final OneDriveItem remoteFile) throws IOException {
        final HttpRequest request = requestFactory.buildDeleteRequest(OneDriveUrl.item(remoteFile.getId()));
        request.execute();
    }

    static class WriteFolderFacet {
        @Key
        private final String name;
        @Key
        private final FolderFacet folder;

        WriteFolderFacet(final String name) {
            this.name = name;
            this.folder = new FolderFacet();
        }

        public String getName() {
            return name;
        }

        public FolderFacet getFolder() {
            return folder;
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

        WriteItemFacet(final String name, final FileSystemInfoFacet fileSystemInfo, final boolean multipart, final boolean isDirectory) {
            this.name = name;
            this.fileSystemInfo = fileSystemInfo;
            this.multipart = multipart ? "cid:content" : null;

            if (isDirectory) {
                this.folder = new FolderFacet();
            } else {
                this.file = new FileFacet();
            }
        }
    }

    static final class UploadSessionFacet {

        @Key
        private final FileDetail item;

        private UploadSessionFacet(final String name) {
            this.item = new FileDetail(name);
        }

        public FileDetail getItem() {
            return item;
        }

        public static class FileDetail {

            @Key
            private final String name;

            @Key("@name.conflictBehavior")
            private final String conflictBehavior = "replace";

            public FileDetail(final String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }
    }
}
