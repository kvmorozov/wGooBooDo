package ru.kmorozov.db.core.config;

import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

public class DbStorage implements IStorage {

    @Override
    public boolean isValidOrCreate() {
        return false;
    }

    @Override
    public IStorage getChildStorage(IBookData bookData) throws IOException {
        return null;
    }

    @Override
    public int size() throws IOException {
        return 0;
    }

    @Override
    public Set<String> getBookIdsList() throws IOException {
        return null;
    }

    @Override
    public boolean isPageExists(IPage page) throws IOException {
        return false;
    }

    @Override
    public Stream<IStoredItem> getItems() throws IOException {
        return null;
    }

    @Override
    public IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public IIndex getIndex(String indexName, boolean createIfNotExists) {
        return null;
    }

    @Override
    public void restoreState(IBookInfo bookInfo) {

    }
}
