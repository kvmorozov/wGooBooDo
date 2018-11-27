package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class Category {

    @Id
    String id;

    @Indexed(unique = true)
    String name;

    @DBRef(lazy = true)
    Set<Category> parents;

    @DBRef(lazy = true)
    Set<Storage> storages;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Category> getParents() {
        return this.parents;
    }

    public void setParents(Set<Category> parents) {
        this.parents = parents;
    }

    public Iterable<Storage> getStorages() {
        return this.storages;
    }

    public void setStorages(Set<Storage> storages) {
        this.storages = storages;
    }

    public void addStorage(Storage storage) {
        if (null == this.storages)
            this.storages = new HashSet<>();

        this.storages.add(storage);
    }

    public void addParents(Collection<Category> items) {
        if (null == this.parents)
            this.parents = new HashSet<>();

        this.parents.addAll(items);
    }

    public void addParent(Category parent) {
        if (null == this.parents)
            this.parents = new HashSet<>();

        this.parents.add(parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || this.getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return this.id.equals(category.id);

    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
