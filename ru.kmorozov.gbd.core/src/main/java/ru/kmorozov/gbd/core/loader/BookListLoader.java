package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class BookListLoader extends DirContextLoader {

    protected static final Logger logger = Logger.getLogger(BookListLoader.class);

    public static final String INDEX_FILE_NAME = "books.index";
    private boolean loadedFromIndex;

    private Set<String> bookIds;

    @Override
    public Set<String> getBookIdsList() {
        return bookIds;
    }

    private Collection<String> loadFromIndex(final File indexFile) {
        Set<String> bookIdsList = new HashSet<>();

        try (Stream<String> idsStream = Files.lines(indexFile.toPath())) {
            bookIdsList = idsStream.filter(BookListLoader::isValidId).collect(Collectors.toSet());
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }

        return bookIdsList;
    }

    @Override
    public void updateIndex() {
        if (loadedFromIndex || !StringUtils.isEmpty(GBDOptions.getBookId())) return;

        try (PrintWriter writer = new PrintWriter(getFileToLoad(true))) {
            ExecutionContext.INSTANCE.getBookIds().forEach(writer::println);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getLoadedFileName() {
        return INDEX_FILE_NAME;
    }

    private static boolean isValidId(final String bookId) {
        return LibraryFactory.isValidId(bookId);
    }

    @Override
    public void refreshContext() {
        super.refreshContext();

        final File indexFile = getFileToLoad(false);
        try {
            bookIds = GBDOptions.getStorage().getBookIdsList();
            if (null != indexFile) {
                loadedFromIndex = true;
                bookIds.addAll(loadFromIndex(indexFile));
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
