package ru.kmorozov.db.core.logic.model.book.rfbr;

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;

public class RfbrBookData implements IBookData {

    private final String bookId;

    public RfbrBookData(final String bookId) {
        this.bookId = bookId;
    }

    @Override
    public String getTitle() {
        return this.bookId;
    }

    @Override
    public String getVolumeId() {
        return this.bookId;
    }
}
