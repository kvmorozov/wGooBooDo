package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public class FileContextProvider extends AbstractContextProvider {

    @Override
    public void updateIndex() {
        BookListLoader.BOOK_LIST_LOADER.updateIndex();
    }

    @Override
    public void updateContext() {
        BookContextLoader.BOOK_CTX_LOADER.updateContext();
    }

    @Override
    public BookInfo getBookInfo(String bookId) {
        return BookContextLoader.BOOK_CTX_LOADER.getBookInfo(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return BookListLoader.BOOK_LIST_LOADER.getBookIdsList();
    }

    @Override
    public int getContextSize() {
        return BookContextLoader.BOOK_CTX_LOADER.getContextSize();
    }

    @Override
    public void refreshContext() {
        BookContextLoader.BOOK_CTX_LOADER.refreshContext();
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
