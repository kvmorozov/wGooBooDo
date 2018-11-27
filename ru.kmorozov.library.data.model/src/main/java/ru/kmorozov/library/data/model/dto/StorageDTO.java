package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.book.Storage.StorageType;

import java.util.Collection;
import java.util.Set;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public class StorageDTO {

    private String id;
    private StorageType storageType;
    private String url, displayName;
    private String parentId;
    private long filesCount, lastChecked;
    private Set<Category> categories;

    public StorageDTO() {
    }

    public StorageDTO(final Storage storage) {
        this(storage, false);
    }

    public StorageDTO(final Storage storage, final boolean withCategories) {
        this.id = storage.getId();
        this.storageType = storage.getStorageType();
        this.url = storage.getUrl();
        this.displayName = storage.getName();
        this.parentId = null == storage.getParent() ? null : storage.getParent().getId();
        this.filesCount = null == storage.getStorageInfo() ? 0L : storage.getStorageInfo().getFilesCount();
        this.lastChecked = null == storage.getStorageInfo() ? 0L : storage.getStorageInfo().getLastChecked();

        if (withCategories)
            this.categories = storage.getCategories();
    }

    public String getId() {
        return id;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getUrl() {
        return url;
    }

    public String getParentId() {
        return parentId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public Collection<Category> getCategories() {
        return categories;
    }

    public long getLastChecked() {
        return lastChecked;
    }
}
