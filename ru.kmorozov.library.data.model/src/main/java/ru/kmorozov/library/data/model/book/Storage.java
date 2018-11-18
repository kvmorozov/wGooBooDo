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

    String localPath;

    public StorageType getStorageType() {
        return storageType;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setStorageType(final StorageType storageType) {
        this.storageType = storageType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Storage getParent() {
        return parent;
    }

    public void setParent(final Storage parent) {
        this.parent = parent;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(final Set<Category> categories) {
        this.categories = categories;
    }

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(final Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void addCategory(final Category category) {
        if (null == categories)
            categories = new HashSet<>();

        categories.add(category);
    }

    public StorageInfo getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(final StorageInfo storageInfo) {
        this.storageInfo = storageInfo;
    }

    public Category getMainCategory() {
        return CollectionUtils.isEmpty(categories) ? null : (Category) categories.toArray()[0];
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        final Storage storage = (Storage) o;

        return id.equals(storage.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }
}
