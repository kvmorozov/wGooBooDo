package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class Storage {

    public enum StorageType {
        LocalFileSystem;
    }

    @Id
    String id;

    StorageType storageType;
    String url;

    @DBRef(lazy = true)
    Storage parent;

    @DBRef(lazy = true)
    List<Category> categories;

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

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        if (categories == null)
            categories = new ArrayList<>();

        categories.add(category);
    }
}
