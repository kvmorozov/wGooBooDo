package ru.kmorozov.library.data.loader.impl;

import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.utils.BookUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

/**
 * Created by sbt-morozov-kv on 16.03.2017.
 */
public class ServerItem {

    private final boolean isDirectory;
    private final String url;
    private final String name;
    private ServerItem parent;
    private Date lastModifiedDateTime;
    private final Storage.StorageType storageType;
    private long filesCount;
    private final long size;
    private final Object originalItem;

    private ServerItem(OneDriveItem oneDriveItem, boolean lookupParent) {
        isDirectory = oneDriveItem.isDirectory();
        url = oneDriveItem.getId();
        name = oneDriveItem.getName();
        lastModifiedDateTime = oneDriveItem.getLastModifiedDateTime();
        storageType = Storage.StorageType.OneDrive;
        originalItem = oneDriveItem;
        size = oneDriveItem.getSize();

        if (lookupParent && null != oneDriveItem.getParent() && null != oneDriveItem.getParent().getId())
            this.parent = new ServerItem(oneDriveItem.getParent(), false);

        if (null != oneDriveItem.getFolder())
            this.filesCount = oneDriveItem.getFolder().getChildCount();
    }

    ServerItem(OneDriveItem oneDriveItem) {
        this(oneDriveItem, true);
    }

    private ServerItem(Path path, boolean lookupParent) {
        File file = path.toFile();

        isDirectory = file.isDirectory();
        url = path.toString();
        name = file.getName();
        storageType = Storage.StorageType.LocalFileSystem;
        originalItem = path;
        size = file.length();

        if (lookupParent)
            this.parent = new ServerItem(path.getParent(), false);
    }

    ServerItem(Path path) {
        this(path, true);
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public ServerItem getParent() {
        return this.parent;
    }

    public Date getLastModifiedDateTime() {
        return this.lastModifiedDateTime;
    }

    public boolean isLoadableItem() {
        if (this.isDirectory)
            return true;
        else {
            BookInfo.BookFormat format = BookUtils.getFormat(this.name);
            return BookInfo.BookFormat.UNKNOWN != format && BookInfo.BookFormat.LNK != format;
        }
    }

    public boolean isLink() {
        return BookInfo.BookFormat.LNK == BookUtils.getFormat(this.name);
    }

    public boolean isLoadableOrLink() {
        return this.isLoadableItem() || this.isLink();
    }

    public Storage.StorageType getStorageType() {
        return this.storageType;
    }

    public long getFilesCount() {
        return this.filesCount;
    }

    public Object getOriginalItem() {
        return this.originalItem;
    }

    public long getSize() {
        return this.size;
    }
}
