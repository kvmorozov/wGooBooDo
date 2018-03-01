package ru.kmorozov.library.data.loader;

import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;
import ru.kmorozov.library.data.model.book.Storage.StorageType;
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
    private final StorageType storageType;
    private long filesCount;
    private final long size;
    private final Object originalItem;

    private ServerItem(final OneDriveItem oneDriveItem, final boolean lookupParent) {
        this.isDirectory = oneDriveItem.isDirectory();
        this.url = oneDriveItem.getId();
        this.name = oneDriveItem.getName();
        this.lastModifiedDateTime = oneDriveItem.getLastModifiedDateTime();
        this.storageType = StorageType.OneDrive;
        this.originalItem = oneDriveItem;
        this.size = oneDriveItem.getSize();

        if (lookupParent && null != oneDriveItem.getParent() && null != oneDriveItem.getParent().getId())
            parent = new ServerItem(oneDriveItem.getParent(), false);

        if (null != oneDriveItem.getFolder())
            filesCount = oneDriveItem.getFolder().getChildCount();
    }

    ServerItem(final OneDriveItem oneDriveItem) {
        this(oneDriveItem, true);
    }

    private ServerItem(final Path path, final boolean lookupParent) {
        final File file = path.toFile();

        this.isDirectory = file.isDirectory();
        this.url = path.toString();
        this.name = file.getName();
        this.storageType = StorageType.LocalFileSystem;
        this.originalItem = path;
        this.size = file.length();

        if (lookupParent)
            parent = new ServerItem(path.getParent(), false);
    }

    ServerItem(final Path path) {
        this(path, true);
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public ServerItem getParent() {
        return parent;
    }

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public boolean isLoadableItem() {
        if (isDirectory)
            return true;
        else {
            final BookFormat format = BookUtils.getFormat(name);
            return BookInfo.BookFormat.UNKNOWN != format && BookInfo.BookFormat.LNK != format;
        }
    }

    public boolean isLink() {
        return BookInfo.BookFormat.LNK == BookUtils.getFormat(name);
    }

    public boolean isLoadableOrLink() {
        return isLoadableItem() || isLink();
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public Object getOriginalItem() {
        return originalItem;
    }

    public long getSize() {
        return size;
    }
}
