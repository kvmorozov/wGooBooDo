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
public class Category {

    @Id
    String id;

    String name;

    @DBRef(lazy = true)
    List<Category> parents;

    @DBRef(lazy = true)
    List<Storage> storages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Category> getParents() {
        return parents;
    }

    public void setParents(List<Category> parents) {
        this.parents = parents;
    }

    public List<Storage> getStorages() {
        return storages;
    }

    public void setStorages(List<Storage> storages) {
        this.storages = storages;
    }

    public void addStorage(Storage storage) {
        if (storages == null)
            storages = new ArrayList<>();

        storages.add(storage);
    }

    public void addParents(List<Category> parents) {
        if (parents == null)
            parents = new ArrayList<>();

        parents.addAll(parents);
    }

    public void addParent(Category parent) {
        if (parents == null)
            parents = new ArrayList<>();

        parents.add(parent);
    }
}
