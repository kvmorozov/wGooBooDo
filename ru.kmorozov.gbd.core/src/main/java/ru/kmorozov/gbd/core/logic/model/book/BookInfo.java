package ru.kmorozov.gbd.core.logic.model.book;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable {

    private BookData bookData;
    private PagesInfo pages;
    private String bookId;

    private long lastPdfChecked;

    public BookInfo() {}

    public BookInfo(BookData bookData, PagesInfo pages, String bookId) {
        this.bookData = bookData;
        this.pages = pages;
        this.bookId = bookId;
    }

    public BookData getBookData() {
        return bookData;
    }

    public PagesInfo getPages() {
        return pages;
    }

    public String getBookId() {
        return bookId;
    }

    public long getLastPdfChecked() {
        return lastPdfChecked;
    }

    public void setLastPdfChecked(long lastPdfChecked) {
        this.lastPdfChecked = lastPdfChecked;
    }
}
