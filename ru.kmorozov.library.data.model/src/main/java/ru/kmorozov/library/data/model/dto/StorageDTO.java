package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public class StorageDTO {

    private String id;
    private Storage.StorageType storageType;
    private String url, displayName;
    private String parentId;
    private long filesCount, lastChecked;
    private Set<Category> categories;

    public StorageDTO() {
    }

    public StorageDTO(Storage storage) {
        this(storage, false);
    }

    public StorageDTO(Storage storage, boolean withCategories) {
        this.id = storage.getId();
        this.storageType = storage.getStorageType();
        this.url = storage.getUrl();
        this.displayName = storage.getName();
        this.parentId = storage.getParent() == null ? null : storage.getParent().getId();
        this.filesCount = storage.getStorageInfo() == null ? 0l : storage.getStorageInfo().getFilesCount();
        this.lastChecked = storage.getStorageInfo() == null ? 0l : storage.getStorageInfo().getLastChecked();

        if (withCategories)
            this.categories = storage.getCategories();
    }

    public String getId() {
        return id;
    }

    public Storage.StorageType getStorageType() {
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

    public Set<Category> getCategories() {
        return categories;
    }

    public long getLastChecked() {
        return lastChecked;
    }
}
