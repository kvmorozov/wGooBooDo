package ru.kmorozov.gbd.core.logic.model.book.base;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public interface IPagesInfo {

    AbstractPage[] getPages();

    String getMissingPagesList();

    void build();

    IPage getPageByPid(String pid);
}
