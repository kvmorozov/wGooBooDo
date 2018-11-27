package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.utils.BookUtils;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class BookDTO {

    private String id;
    private BookInfo.BookFormat format;
    private String title, path, localPath, size;
    private boolean loaded;

    public BookDTO() {
    }

    public BookDTO(Book book, boolean loaded) {
        id = book.getBookId();
        format = book.getBookInfo().getFormat();
        title = null == book.getTitle() ? book.getBookInfo().getFileName() : book.getTitle();
        localPath = book.getBookInfo().getFileName();
        this.loaded = loaded;
        size = BookUtils.humanReadableByteCount(book.getBookInfo().getSize(), true);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BookInfo.BookFormat getFormat() {
        return this.format;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setFormat(BookInfo.BookFormat format) {
        this.format = format;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
