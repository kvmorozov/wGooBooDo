package ru.kmorozov.library.data.loader.impl;

import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;

import java.io.IOException;

/**
 * Created by km on 26.12.2016.
 */

@Component
public class LocalDirectoryLoader extends BaseLoader {

    private static final Logger logger = Logger.getLogger(LocalDirectoryLoader.class);

    @Override
    public Storage refresh(Storage storage) {
        return storage;
    }

    @Override
    public void load() throws IOException {

    }

    @Override
    public void resolveLink(Book lnkBook) {

    }

    @Override
    public void downloadBook(Book book) {

    }

    public void processLinks() {
    }
}