package ru.kmorozov.gbd.core.logic.model.book.base;

import ru.kmorozov.gbd.logger.Logger;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public interface IPagesInfo {

    AbstractPage[] getPages();

    String getMissingPagesList();

    void build(Logger logger);

    IPage getPageByPid(String pid);
}
