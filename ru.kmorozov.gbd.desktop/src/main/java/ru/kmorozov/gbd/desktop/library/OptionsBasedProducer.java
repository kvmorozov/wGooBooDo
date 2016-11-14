package ru.kmorozov.gbd.desktop.library;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;

import java.util.Collections;
import java.util.List;

import static ru.kmorozov.gbd.core.config.storage.BookListLoader.BOOK_LIST_LOADER;

/**
 * Created by km on 12.11.2016.
 */
public class OptionsBasedProducer implements IBookListProducer {

    private List<String> bookIdsList;

    public OptionsBasedProducer() {
        String bookId = GBDOptions.getBookId();
        String bookDirName = GBDOptions.getOutputDir();

        if (bookId != null && bookId.length() > 0 && BOOK_LIST_LOADER.isValidId(bookId))
            bookIdsList = Collections.singletonList(bookId);
        else if (bookDirName != null && bookDirName.length() > 0) {
            if (BOOK_LIST_LOADER.isValidDirectory()) {
                bookIdsList = BOOK_LIST_LOADER.getBookIdsList();
            }
        }

        if (bookIdsList == null || bookIdsList.size() == 0) throw new RuntimeException("No books to load!");
    }

    @Override
    public List<String> getBookIds() {
        return bookIdsList;
    }
}
