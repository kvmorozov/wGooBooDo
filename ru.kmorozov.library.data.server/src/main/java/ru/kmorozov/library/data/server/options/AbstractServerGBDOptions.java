package ru.kmorozov.library.data.server.options;

import ru.kmorozov.gbd.core.config.IGBDOptions;

public abstract class AbstractServerGBDOptions implements IGBDOptions {

    protected String bookId;

    @Override
    public String getBookId() {
        return bookId;
    }

    @Override
    public String getProxyListFile() {
        return null;
    }

    @Override
    public int getImageWidth() {
        return 0;
    }

    @Override
    public boolean reloadImages() {
        return false;
    }

    @Override
    public boolean secureMode() {
        return false;
    }

    @Override
    public String pdfOptions() {
        return null;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

}
