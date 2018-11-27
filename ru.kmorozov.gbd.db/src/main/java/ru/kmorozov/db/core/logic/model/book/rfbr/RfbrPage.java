package ru.kmorozov.db.core.logic.model.book.rfbr;

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

public class RfbrPage extends AbstractPage {

    private final String bookId;
    private final int order;

    public RfbrPage(String bookId, int order) {
        this.bookId = bookId;
        this.order = order;
    }


    @Override
    public String getPid() {
        return String.valueOf(order);
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    @Override
    public String getImgUrl() {
        return String.format("http://www.rfbr.ru/rffi/djvu_page?objectId=%s&width=1000&page=%d", bookId, order);
    }
}
