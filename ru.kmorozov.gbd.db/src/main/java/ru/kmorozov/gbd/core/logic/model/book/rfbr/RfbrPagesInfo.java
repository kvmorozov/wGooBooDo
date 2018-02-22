package ru.kmorozov.gbd.core.logic.model.book.rfbr;

import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.logger.Logger;

public class RfbrPagesInfo implements IPagesInfo {

    private final RfbrPage[] pages;

    public RfbrPagesInfo(final RfbrPage[] pages) {
        this.pages = pages;
    }


    @Override
    public AbstractPage[] getPages() {
        return pages;
    }

    @Override
    public String getMissingPagesList() {
        return null;
    }

    @Override
    public void build(Logger logger) {

    }

    @Override
    public IPage getPageByPid(String pid) {
        return null;
    }
}
