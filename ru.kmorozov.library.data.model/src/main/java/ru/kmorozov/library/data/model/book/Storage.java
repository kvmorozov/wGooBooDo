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

    StorageType storageType;
    String url, name;
    Date lastModifiedDateTime;

    @DBRef(lazy = true)
    Storage parent;

    @DBRef(lazy = true)
    Set<Category> categories;

    StorageInfo storageInfo;

    public StorageType getStorageType() {
        return storageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Storage getParent() {
        return parent;
    }

    public void setParent(Storage parent) {
        this.parent = parent;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCategory(Category category) {
        if (categories == null)
            categories = new HashSet<>();

        if (!categories.contains(category))
            categories.add(category);
    }

    public StorageInfo getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(StorageInfo storageInfo) {
        this.storageInfo = storageInfo;
    }

    public Category getMainCategory() {
        return CollectionUtils.isEmpty(categories) ? null : (Category) categories.toArray()[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Storage storage = (Storage) o;

        return id.equals(storage.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
