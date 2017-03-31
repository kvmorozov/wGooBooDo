package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class Category {

    @Id
    String id;

    String name;

    @DBRef(lazy = true)
    Set<Category> parents;

    @DBRef(lazy = true)
    Set<Storage> storages;

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

    public Set<Category> getParents() {
        return parents;
    }

    public void setParents(Set<Category> parents) {
        this.parents = parents;
    }

    public Set<Storage> getStorages() {
        return storages;
    }

    public void setStorages(Set<Storage> storages) {
        this.storages = storages;
    }

    public void addStorage(Storage storage) {
        if (storages == null)
            storages = new HashSet<>();

        storages.add(storage);
    }

    public void addParents(Set<Category> items) {
        if (parents == null)
            parents = new HashSet<>();

        parents.addAll(items);
    }

    public void addParent(Category parent) {
        if (parents == null)
            parents = new HashSet<>();

        if (!parents.contains(parent))
            parents.add(parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return id.equals(category.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
