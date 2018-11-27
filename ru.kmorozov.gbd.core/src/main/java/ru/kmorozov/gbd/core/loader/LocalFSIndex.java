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

    public LocalFSIndex(final LocalFSStorage storage, final String indexName, final boolean createIfNotExists) {
        this.storage = storage;

        this.indexFile = new File(storage.getStorageDir().getPath() + File.separator + indexName);
        if (!this.indexFile.exists() && createIfNotExists) {
            try {
                this.indexFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBookInfo[] getBooks() {
        BookInfo[] ctxObjArr = null;
        try (final FileReader reader = new FileReader(this.indexFile)) {
            ctxObjArr = Mapper.getGson().fromJson(reader, BookInfo[].class);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return ctxObjArr;
    }

    @Override
    public void updateIndex(final List<IBookInfo> books) {
        try {
            try (final FileWriter writer = new FileWriter(this.indexFile)) {
                Mapper.getGson().toJson(books, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
