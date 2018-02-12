package ru.kmorozov.gbd.desktop.library;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.ContextProvider;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by km on 12.11.2016.
 */
public class OptionsBasedProducer implements IBookListProducer {

    private Set<String> bookIdsList;

    public OptionsBasedProducer() {
        final String bookId = GBDOptions.getBookId();
        final String bookDirName = GBDOptions.getOutputDir();

        if (null != bookId && !bookId.isEmpty() && LibraryFactory.isValidId(bookId))
            bookIdsList = new HashSet<>(Collections.singletonList(bookId));
        else if (null != bookDirName && !bookDirName.isEmpty()) {
            if (GBDOptions.isValidDirectory()) {
                bookIdsList = ContextProvider.getContextProvider().getBookIdsList();
            }
        }

        if (null == bookIdsList || bookIdsList.isEmpty()) throw new RuntimeException("No books to load!");
    }

    @Override
    public Set<String> getBookIds() {
        return bookIdsList;
    }
}
