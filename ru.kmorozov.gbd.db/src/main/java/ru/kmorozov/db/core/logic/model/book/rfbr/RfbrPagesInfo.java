package ru.kmorozov.db.core.logic.model.book.rfbr;

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;

public class RfbrPagesInfo implements IPagesInfo {

    private final RfbrPage[] pages;

    public RfbrPagesInfo(RfbrPage[] pages) {
        this.pages = pages;
    }


    @Override
    public AbstractPage[] getPages() {
        return this.pages;
    }

    @Override
    public String getMissingPagesList() {
        return null;
    }

    @Override
    public void build() {

    }

    @Override
    public IPage getPageByPid(final String pid) {
        return null;
    }
}
