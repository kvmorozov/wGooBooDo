package ru.kmorozov.gbd.core.config;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public interface IBaseLoader {

    void updateIndex();

    void updateContext();

    void updateBookInfo(BookInfo bookInfo);

    BookInfo getBookInfo(String bookId);

    Set<String> getBookIdsList();

    int getContextSize();

    void refreshContext();

    boolean isValid();
}
