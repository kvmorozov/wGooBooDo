package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Storage;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */
public class StorageDTO {

    private String id;
    private Storage.StorageType storageType;
    private String url;
    private String parentId;

    public StorageDTO() {}

    public StorageDTO(Storage storage) {
        this.id = storage.getId();
        this.storageType = storage.getStorageType();
        this.url = storage.getUrl();
        this.parentId = storage.getParent() == null ? null : storage.getParent().getId();
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
}
