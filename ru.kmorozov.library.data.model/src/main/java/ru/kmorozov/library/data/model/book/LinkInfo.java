package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Created by sbt-morozov-kv on 11.04.2017.
 */
public class LinkInfo {

    @DBRef
    Storage linkedStorage;

    @DBRef
    Book linkedBook;

    public Storage getLinkedStorage() {
        return linkedStorage;
    }

    public void setLinkedStorage(Storage linkedStorage) {
        this.linkedStorage = linkedStorage;
    }

    public Book getLinkedBook() {
        return linkedBook;
    }

    public void setLinkedBook(Book linkedBook) {
        this.linkedBook = linkedBook;
    }
}
