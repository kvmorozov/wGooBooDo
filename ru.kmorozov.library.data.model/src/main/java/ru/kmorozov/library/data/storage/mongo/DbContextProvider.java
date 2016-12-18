package ru.kmorozov.library.data.storage.mongo;

import ru.kmorozov.gbd.core.config.storage.AbstractContextProvider;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public class DbContextProvider extends AbstractContextProvider {

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {

    }

    @Override
    public BookInfo getBookInfo(String bookId) {
        return null;
    }

    @Override
    public List<String> getBookIdsList() {
        return null;
    }

    @Override
    public int getContextSize() {
        return 0;
    }

    @Override
    public void refreshContext() {

    }

    @Override
    public boolean isValid() {
        return false;
    }
}
