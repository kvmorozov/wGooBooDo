package ru.kmorozov.library.data.server;

import ru.kmorozov.gbd.core.config.IStorage;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public class ServerStorage implements IStorage {

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IStorage getChildStorage(IBookData bookData) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<String> getBookIdsList() {
        return null;
    }

    @Override
    public boolean isPageExists(IPage page) throws IOException {
        return false;
    }

    @Override
    public Stream<Path> getFiles() throws IOException {
        return null;
    }

    @Override
    public IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException {
        return null;
    }
}
