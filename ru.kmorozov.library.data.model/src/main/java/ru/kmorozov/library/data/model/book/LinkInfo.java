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
        return this.linkedStorage;
    }

    public void setLinkedStorage(Storage linkedStorage) {
        this.linkedStorage = linkedStorage;
    }

    public Book getLinkedBook() {
        return this.linkedBook;
    }

    public void setLinkedBook(Book linkedBook) {
        this.linkedBook = linkedBook;
    }

    public boolean isBroken() {
        return this.broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
