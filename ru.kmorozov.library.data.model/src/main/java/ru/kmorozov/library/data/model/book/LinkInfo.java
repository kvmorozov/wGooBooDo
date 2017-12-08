package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Created by sbt-morozov-kv on 11.04.2017.
 */
public class LinkInfo {

    boolean broken;

    @DBRef
    Storage linkedStorage;

    @DBRef
    Book linkedBook;

    public Storage getLinkedStorage() {
        return linkedStorage;
    }

    public void setLinkedStorage(final Storage linkedStorage) {
        this.linkedStorage = linkedStorage;
    }

    public Book getLinkedBook() {
        return linkedBook;
    }

    public void setLinkedBook(final Book linkedBook) {
        this.linkedBook = linkedBook;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(final boolean broken) {
        this.broken = broken;
    }
}
