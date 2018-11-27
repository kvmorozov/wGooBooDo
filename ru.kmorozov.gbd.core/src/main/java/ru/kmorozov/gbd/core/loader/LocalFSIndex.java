package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;
import ru.kmorozov.db.utils.Mapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LocalFSIndex implements IIndex {

    private final File indexFile;
    private final LocalFSStorage storage;

    public LocalFSIndex(LocalFSStorage storage, String indexName, boolean createIfNotExists) {
        this.storage = storage;

        indexFile = new File(storage.getStorageDir().getPath() + File.separator + indexName);
        if (!indexFile.exists() && createIfNotExists) {
            try {
                indexFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBookInfo[] getBooks() {
        BookInfo[] ctxObjArr = null;
        try (FileReader reader = new FileReader(indexFile)) {
            ctxObjArr = Mapper.getGson().fromJson(reader, BookInfo[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ctxObjArr;
    }

    @Override
    public void updateIndex(List<IBookInfo> books) {
        try {
            try (FileWriter writer = new FileWriter(indexFile)) {
                Mapper.getGson().toJson(books, writer);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
