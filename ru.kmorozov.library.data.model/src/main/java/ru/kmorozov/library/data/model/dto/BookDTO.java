package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;

/**
 * Created by sbt-morozov-kv on 31.01.2017.
 */
public class BookDTO {

    private String id;
    private BookInfo.BookFormat format;
    private String title;

    public BookDTO() {
    }

    public BookDTO(Book book) {
        this(book, false);
    }

    public BookDTO(Book book, boolean withCategories) {
        this.id = book.getBookId();
        this.format = book.getBookInfo().getFormat();
        this.title = book.getTitle() == null ? book.getBookInfo().getFileName() : book.getTitle();
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

    public void setFormat(BookInfo.BookFormat format) {
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
