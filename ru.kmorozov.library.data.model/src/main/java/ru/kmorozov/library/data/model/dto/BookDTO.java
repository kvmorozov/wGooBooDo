package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;
import ru.kmorozov.library.utils.BookUtils;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class BookDTO {

    private String id;
    private BookFormat format;
    private String title, path, localPath, size;
    private boolean loaded;

    public BookDTO() {
    }

    public BookDTO(final Book book, final boolean loaded) {
        this.id = book.getBookId();
        this.format = book.getBookInfo().getFormat();
        this.title = null == book.getTitle() ? book.getBookInfo().getFileName() : book.getTitle();
        this.localPath = book.getBookInfo().getFileName();
        this.loaded = loaded;
        this.size = BookUtils.humanReadableByteCount(book.getBookInfo().getSize(), true);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public BookFormat getFormat() {
        return format;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setFormat(final BookFormat format) {
        this.format = format;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }
}
