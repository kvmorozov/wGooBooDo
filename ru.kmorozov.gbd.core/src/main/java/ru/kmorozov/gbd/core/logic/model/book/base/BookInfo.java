package ru.kmorozov.gbd.core.logic.model.book.base;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable {

    private IBookData bookData;
    private IPagesInfo pages;
    private String bookId;

    private long lastPdfChecked;

    public BookInfo() {
    }

    public BookInfo(final IBookData bookData, final IPagesInfo pages, final String bookId) {
        this.bookData = bookData;
        this.pages = pages;
        this.bookId = bookId;
    }

    public IBookData getBookData() {
        return bookData;
    }

    public IPagesInfo getPages() {
        return pages;
    }

    public String getBookId() {
        return bookId;
    }

    public long getLastPdfChecked() {
        return lastPdfChecked;
    }

    public void setLastPdfChecked(final long lastPdfChecked) {
        this.lastPdfChecked = lastPdfChecked;
    }
}
