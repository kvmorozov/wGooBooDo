package ru.kmorozov.db.core.config;

import ru.kmorozov.db.core.logic.model.book.BookInfo;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public interface IContextLoader {

    void updateIndex();

    void updateContext();

    void updateBookInfo(BookInfo bookInfo);

    BookInfo getBookInfo(String bookId);

    Set<String> getBookIdsList();

    int getContextSize();

    void refreshContext();

    boolean isValid();
}
