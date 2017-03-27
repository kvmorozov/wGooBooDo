package ru.kmorozov.library.data.loader;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.utils.BookUtils;

import java.nio.file.Path;
import java.util.Date;

/**
 * Created by sbt-morozov-kv on 16.03.2017.
 */
public class ServerItem {

    private boolean isDirectory;
    private String url, name;
    private ServerItem parent;
    private Date lastModifiedDateTime;
    private Storage.StorageType storageType;
    private long filesCount;

    private ServerItem(OneDriveItem oneDriveItem, boolean lookupParent) {
        this.isDirectory = oneDriveItem.isDirectory();
        this.url = oneDriveItem.getId();
        this.name = oneDriveItem.getName();
        this.lastModifiedDateTime = oneDriveItem.getLastModifiedDateTime();
        this.storageType = Storage.StorageType.OneDrive;

        if (lookupParent && oneDriveItem.getParent() != null && oneDriveItem.getParent().getId() != null)
            parent = new ServerItem(oneDriveItem.getParent(), false);

        if (oneDriveItem.getFolder() != null)
            filesCount = oneDriveItem.getFolder().getChildCount();
    }

    ServerItem(OneDriveItem oneDriveItem) {
        this(oneDriveItem, true);
    }

    private ServerItem(Path path, boolean lookupParent) {
        this.isDirectory = path.toFile().isDirectory();
        this.url = path.toString();
        this.name = path.toFile().getName();
        this.storageType = Storage.StorageType.LocalFileSystem;

        if (lookupParent)
            parent = new ServerItem(path.getParent(), false);
    }

    ServerItem(Path path) {
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
        return isDirectory || BookUtils.getFormat(name) != BookInfo.BookFormat.UNKNOWN;
    }

    public Storage.StorageType getStorageType() {
        return storageType;
    }

    public long getFilesCount() {
        return filesCount;
    }
}
