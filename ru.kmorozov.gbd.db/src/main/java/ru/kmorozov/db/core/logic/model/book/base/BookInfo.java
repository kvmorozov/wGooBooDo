package ru.kmorozov.db.core.logic.model.book.base;

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.logger.model.ILoggableObject;

import java.io.Serializable;

/**
 * Created by km on 28.11.2015.
 */
public class BookInfo implements Serializable, ILoggableObject, IBookInfo {

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

    @Override
    public IBookData getBookData() {
        return bookData;
    }

    @Override
    public IPagesInfo getPages() {
        return pages;
    }

    @Override
    public String getBookId() {
        return bookId;
    }

    public long getLastPdfChecked() {
        return lastPdfChecked;
    }

    public void setLastPdfChecked(final long lastPdfChecked) {
        this.lastPdfChecked = lastPdfChecked;
    }

    @Override
    public String getDescription() {
        return getBookData().getTitle();
    }
}
