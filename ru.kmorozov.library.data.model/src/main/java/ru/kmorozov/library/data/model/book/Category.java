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
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Set<Category> getParents() {
        return parents;
    }

    public void setParents(final Set<Category> parents) {
        this.parents = parents;
    }

    public Iterable<Storage> getStorages() {
        return storages;
    }

    public void setStorages(final Set<Storage> storages) {
        this.storages = storages;
    }

    public void addStorage(final Storage storage) {
        if (null == storages)
            storages = new HashSet<>();

        storages.add(storage);
    }

    public void addParents(final Collection<Category> items) {
        if (null == parents)
            parents = new HashSet<>();

        parents.addAll(items);
    }

    public void addParent(final Category parent) {
        if (null == parents)
            parents = new HashSet<>();

        if (!parents.contains(parent))
            parents.add(parent);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        final Category category = (Category) o;

        return id.equals(category.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
