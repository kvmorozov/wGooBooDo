package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class Storage {

    public enum StorageType {
        LocalFileSystem,
        OneDrive
    }

    @Id
    String id;

    Storage.StorageType storageType;
    String url, name;
    Date lastModifiedDateTime;

    @DBRef(lazy = true)
    Storage parent;

    @DBRef(lazy = true)
    Set<Category> categories;

    StorageInfo storageInfo;

    String localPath;

    public Storage.StorageType getStorageType() {
        return this.storageType;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStorageType(Storage.StorageType storageType) {
        this.storageType = storageType;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Storage getParent() {
        return this.parent;
    }

    public void setParent(Storage parent) {
        this.parent = parent;
    }

    public Set<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Date getLastModifiedDateTime() {
        return this.lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCategory(Category category) {
        if (null == this.categories)
            this.categories = new HashSet<>();

        this.categories.add(category);
    }

    public StorageInfo getStorageInfo() {
        return this.storageInfo;
    }

    public void setStorageInfo(StorageInfo storageInfo) {
        this.storageInfo = storageInfo;
    }

    public Category getMainCategory() {
        return CollectionUtils.isEmpty(this.categories) ? null : (Category) this.categories.toArray()[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || this.getClass() != o.getClass()) return false;

        Storage storage = (Storage) o;

        return this.id.equals(storage.id);

    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
