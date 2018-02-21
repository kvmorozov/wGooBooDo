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
        return this.bookIds;
    }

    public void setBookIds(Map<IdType, String> bookIds) {
        this.bookIds = bookIds;
    }

    public Book(final String title, final String author) {
        this.title = title;
        this.author = author;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(final String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(final BookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(final Storage storage) {
        this.storage = storage;
    }

    public String getBookKey() {
        return bookInfo.getFileName();
    }

    public LinkInfo getLinkInfo() {
        return linkInfo;
    }

    public void setLinkInfo(final LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }

    public boolean isLink() {
        return this.bookInfo.isLink();
    }

    public boolean isBrokenLink() {
        return this.bookInfo.isLink() && ((null != this.linkInfo && this.linkInfo.isBroken() || null == this.linkInfo));
    }

    public Set<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        if (null == title) return false;

        final Book book = (Book) o;

        if (!title.equals(book.title)) return false;
        return author.equals(book.author);
    }

    @Override
    public int hashCode() {
        if (null == title)
            return super.hashCode();

        int result = title.hashCode();
        result = 31 * result + author.hashCode();
        return result;
    }

    public void addBookId(IdType idType, String bookId) {
        bookIds = bookIds == null ? new HashMap<>(1) : bookIds;
        bookIds.put(idType, bookId);
    }
}
