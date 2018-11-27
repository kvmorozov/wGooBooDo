package ru.kmorozov.onedrive.client;

import ru.kmorozov.onedrive.client.resources.Drive;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import ru.kmorozov.onedrive.client.downloader.ResumableDownloaderProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface OneDriveProvider {

    // Read only operations

    Drive getDefaultDrive() throws IOException;

    OneDriveItem getRoot() throws IOException;

    OneDriveItem[] getChildren(OneDriveItem parent) throws IOException;

    OneDriveItem[] getChildren(String id) throws IOException;

    OneDriveItem getPath(String path) throws IOException;

    OneDriveItem getItem(String id) throws IOException;

    // Write operations

    OneDriveItem replaceFile(OneDriveItem parent, File file) throws IOException;

    OneDriveItem uploadFile(OneDriveItem parent, File file) throws IOException;

    OneDriveUploadSession startUploadSession(OneDriveItem parent, File file) throws IOException;

    void uploadChunk(OneDriveUploadSession session) throws IOException;

    OneDriveItem updateFile(OneDriveItem item, Date createdDate, Date modifiedDate) throws IOException;

    OneDriveItem createFolder(OneDriveItem parent, String name) throws IOException;

    void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener, int chunkSize) throws IOException;

    void download(OneDriveItem item, File target, ResumableDownloaderProgressListener progressListener) throws IOException;

    void delete(OneDriveItem remoteFile) throws IOException;

    OneDriveItem[] search(String query) throws IOException;

    class FACTORY {

        public static OneDriveProvider readOnlyApi(final AuthorisationProvider authoriser) {
            return new ROOneDriveProvider(authoriser);
        }

        public static OneDriveProvider readWriteApi(final AuthorisationProvider authoriser) {
            return new RWOneDriveProvider(authoriser);
        }
    }
}
