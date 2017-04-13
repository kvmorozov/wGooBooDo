package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Document
public class Book {

    @Id
    String bookId;
    @TextIndexed
    String title;
    @TextIndexed
    String author;

    BookInfo bookInfo;

    LinkInfo linkInfo;

    @DBRef
    Storage storage;

    public Book() {
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(BookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public String getBookKey() {
        return bookInfo.getFileName();
    }

    public LinkInfo getLinkInfo() {
        return linkInfo;
    }

    public void setLinkInfo(LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }

    public boolean isLink() {
        return this.bookInfo.isLink();
    }

    public boolean isBrokenLink() {
        return this.bookInfo.isLink() && ((this.linkInfo != null && this.linkInfo.isBroken() || this.linkInfo == null));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (title == null) return false;

        Book book = (Book) o;

        if (!title.equals(book.title)) return false;
        return author.equals(book.author);
    }

    @Override
    public int hashCode() {
        if (title == null)
            return super.hashCode();

        int result = title.hashCode();
        result = 31 * result + author.hashCode();
        return result;
    }
}
