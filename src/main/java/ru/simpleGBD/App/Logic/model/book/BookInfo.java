package ru.simpleGBD.App.Logic.model.book;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable {

    private BookData bookData;
    private PagesInfo pages;

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
