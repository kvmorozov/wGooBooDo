package ru.simpleGBD.App.Logic.model.book;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable {

    private final BookData bookData;
    private final PagesInfo pages;

    public BookInfo(BookData bookData, PagesInfo pages) {
        this.bookData = bookData;
        this.pages = pages;
    }

    public BookData getBookData() {
        return bookData;
    }

    public PagesInfo getPagesInfo() {
        return pages;
    }
}
