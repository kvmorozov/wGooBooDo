package ru.kmorozov.gbd.desktop.library;

import org.apache.commons.lang3.StringUtils;
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

    private Set<String> bookIdsList = new HashSet<>();

    public OptionsBasedProducer() {
        final String bookId = GBDOptions.getBookId();

        if (!StringUtils.isEmpty(bookId) && LibraryFactory.isValidId(bookId))
            bookIdsList = new HashSet<>(Collections.singletonList(bookId));
        else if (GBDOptions.isValidConfig())
            bookIdsList = ContextProvider.getContextProvider().getBookIdsList();
    }

    @Override
    public Set<String> getBookIds() {
        return bookIdsList;
    }
}
