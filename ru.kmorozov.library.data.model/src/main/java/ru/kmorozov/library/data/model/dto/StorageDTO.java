package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;

import java.util.Collection;
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
        id = storage.getId();
        storageType = storage.getStorageType();
        url = storage.getUrl();
        displayName = storage.getName();
        parentId = null == storage.getParent() ? null : storage.getParent().getId();
        filesCount = null == storage.getStorageInfo() ? 0L : storage.getStorageInfo().getFilesCount();
        lastChecked = null == storage.getStorageInfo() ? 0L : storage.getStorageInfo().getLastChecked();

        if (withCategories)
            categories = storage.getCategories();
    }

    public String getId() {
        return this.id;
    }

    public Storage.StorageType getStorageType() {
        return this.storageType;
    }

    public String getUrl() {
        return this.url;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public long getFilesCount() {
        return this.filesCount;
    }

    public Collection<Category> getCategories() {
        return this.categories;
    }

    public long getLastChecked() {
        return this.lastChecked;
    }
}
