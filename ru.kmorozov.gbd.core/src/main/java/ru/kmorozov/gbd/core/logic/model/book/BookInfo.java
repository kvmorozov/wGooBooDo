package ru.kmorozov.gbd.core.logic.model.book;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable {

    private final BookData bookData;
    private final PagesInfo pages;
    private final String bookId;

    public BookInfo(BookData bookData, PagesInfo pages, String bookId) {
        this.bookData = bookData;
        this.pages = pages;
        this.bookId = bookId;
    }

    public BookData getBookData() {
        return bookData;
    }

    public PagesInfo getPagesInfo() {
        return pages;
    }

    public String getBookId() {
        return bookId;
    }
}
