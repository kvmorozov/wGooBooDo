package ru.kmorozov.gbd.core.logic.model.book.base;

import ru.kmorozov.gbd.core.logic.model.book.google.GogglePageInfo;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public interface IPagesInfo {

    GogglePageInfo[] getPages();

    String getMissingPagesList();

    void build();

    GogglePageInfo getPageByPid(String pid);
}
