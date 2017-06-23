package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class BookDTO {

    private String id;
    private BookInfo.BookFormat format;
    private String title, path, localPath;

    public BookDTO() {
    }

    public BookDTO(Book book) {
        this(book, false);
    }

    public BookDTO(Book book, boolean withCategories) {
        this.id = book.getBookId();
        this.format = book.getBookInfo().getFormat();
        this.title = book.getTitle() == null ? book.getBookInfo().getFileName() : book.getTitle();
        this.localPath = book.getStorage().getLocalPath();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BookInfo.BookFormat getFormat() {
        return format;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setFormat(BookInfo.BookFormat format) {
        this.format = format;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
