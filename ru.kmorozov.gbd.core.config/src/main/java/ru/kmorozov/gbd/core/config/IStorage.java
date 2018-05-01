package ru.kmorozov.gbd.core.config;

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public interface IStorage {

    boolean isValidOrCreate();

    IStorage getChildStorage(IBookData bookData) throws IOException;

    int size() throws IOException;

    Set<String> getBookIdsList() throws IOException;

    boolean isPageExists(IPage page) throws IOException;

    Stream<Path> getFiles() throws IOException;

    IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException;

    void refresh();

    IIndex getIndex(String indexName, boolean createIfNotExists);
}
