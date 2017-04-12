package ru.kmorozov.library.data.loader;

import ru.kmorozov.library.data.model.book.Book;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public interface ILoader {

    void load() throws IOException;

    public void resolveLink(Book lnkBook) throws IOException;
}
