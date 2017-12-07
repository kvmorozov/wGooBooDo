package ru.kmorozov.gbd.core.config.storage;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class BookListLoader extends BaseLoader {

    static final BookListLoader BOOK_LIST_LOADER = new BookListLoader();
    private static final String INDEX_FILE_NAME = "books.index";
    private boolean loadedFromIndex = false;

    protected BookListLoader() {
        super();
    }

    public Set<String> getBookIdsList() {
        File indexFile = getFileToLoad(false);
        Set<String> result = loadFromDirNames();
        if (indexFile != null) {
            loadedFromIndex = true;
            result.addAll(loadFromIndex(indexFile));
        }

        return result;
    }

    private Set<String> loadFromDirNames() {
        Set<String> bookIdsList = new HashSet<>();
        try {
            Files.walk(Paths.get(GBDOptions.getBooksDir().toURI())).forEach(filePath -> {
                if (filePath.toFile().isDirectory()) {
                    String[] nameParts = filePath.toFile().getName().split(" ");
                    if (isValidId(nameParts[nameParts.length - 1])) bookIdsList.add(nameParts[nameParts.length - 1]);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bookIdsList;
    }

    private Set<String> loadFromIndex(File indexFile) {
        Set<String> bookIdsList = new HashSet<>();

        try (Stream<String> idsStream = Files.lines(indexFile.toPath())) {
            bookIdsList = idsStream.filter(BookListLoader::isValidId).collect(Collectors.toSet());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return bookIdsList;
    }

    private static boolean isValidId(String bookId) {
        return LibraryFactory.isValidId(bookId);
    }

    public void updateIndex() {
        if (loadedFromIndex || !StringUtils.isEmpty(GBDOptions.getBookId())) return;

        try (PrintWriter writer = new PrintWriter(getFileToLoad(true))) {
            ExecutionContext.INSTANCE.getBookIds().forEach(writer::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getLoadedFileName() {
        return INDEX_FILE_NAME;
    }
}
