package ru.kmorozov.library.data.model.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @DBRef(lazy = true)
    Set<Category> categories;

    Map<IdType, String> bookIds;

    public Book() {
    }

    public Map<IdType, String> getBookIds() {
        return bookIds;
    }

    public void setBookIds(final Map<IdType, String> bookIds) {
        this.bookIds = bookIds;
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getBookId() {
        return this.bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BookInfo getBookInfo() {
        return this.bookInfo;
    }

    public void setBookInfo(BookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public String getBookKey() {
        return this.bookInfo.getFileName();
    }

    public LinkInfo getLinkInfo() {
        return this.linkInfo;
    }

    public void setLinkInfo(LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }

    public boolean isLink() {
        return bookInfo.isLink();
    }

    public boolean isBrokenLink() {
        return bookInfo.isLink() && ((null == linkInfo || linkInfo.isBroken()));
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(final Set<Category> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || this.getClass() != o.getClass()) return false;
        if (null == this.title) return false;

        Book book = (Book) o;

        if (!this.title.equals(book.title)) return false;
        return this.author.equals(book.author);
    }

    @Override
    public int hashCode() {
        if (null == this.title)
            return super.hashCode();

        int result = this.title.hashCode();
        result = 31 * result + this.author.hashCode();
        return result;
    }

    public void addBookId(final IdType idType, final String bookId) {
        this.bookIds = this.bookIds == null ? new HashMap<>(1) : this.bookIds;
        this.bookIds.put(idType, bookId);
    }
}
