package ru.kmorozov.gbd.core.logic.model.book.shpl;

import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GogglePageInfo;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPagesInfo implements IPagesInfo {

    private ShplPage[] pages;

    public ShplPagesInfo(ShplPage[] pages) {
        this.pages = pages;
    }

    @Override
    public GogglePageInfo[] getPages() {
        return new GogglePageInfo[0];
    }

    @Override
    public String getMissingPagesList() {
        return null;
    }

    @Override
    public void build() {

    }

    @Override
    public GogglePageInfo getPageByPid(String pid) {
        return null;
    }
}
