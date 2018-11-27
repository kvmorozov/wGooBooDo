package ru.kmorozov.db.core.logic.model.book;

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

    public BookInfo(IBookData bookData, IPagesInfo pages, String bookId) {
        this.bookData = bookData;
        this.pages = pages;
        this.bookId = bookId;
    }

    @Override
    public IBookData getBookData() {
        return this.bookData;
    }

    @Override
    public IPagesInfo getPages() {
        return this.pages;
    }

    @Override
    public String getBookId() {
        return this.bookId;
    }

    public long getLastPdfChecked() {
        return this.lastPdfChecked;
    }

    public void setLastPdfChecked(long lastPdfChecked) {
        this.lastPdfChecked = lastPdfChecked;
    }

    @Override
    public String getDescription() {
        return this.bookData.getTitle();
    }
}
