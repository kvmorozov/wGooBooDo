package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.logic.context.ExecutionContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class BookListLoader extends BaseLoader {

    private static final String INDEX_FILE_NAME = "books.index";

    public static final BookListLoader BOOK_LIST_LOADER = new BookListLoader();

    private boolean loadedFromIndex = false;

    protected BookListLoader() {
        super();
    }

    public List<String> getBookIdsList() {
        File indexFile = getFileToLoad(false);
        if (indexFile == null)
            return loadFromDirNames();
        else {
            loadedFromIndex = true;
            return loadFromIndex(indexFile);
        }
    }

    private List<String> loadFromDirNames() {
        List<String> bookIdsList = new ArrayList<>();
        try {
            Files.walk(Paths.get(booksDir.toURI())).forEach(filePath -> {
                if (filePath.toFile().isDirectory()) {
                    String[] nameParts = filePath.toFile().getName().split(" ");
                    if (isValidId(nameParts[nameParts.length - 1]))
                        bookIdsList.add(nameParts[nameParts.length - 1]);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bookIdsList;
    }

    private List<String> loadFromIndex(File indexFile) {
        List<String> bookIdsList = null;

        try (Stream<String> idsStream = Files.lines(indexFile.toPath())) {
            bookIdsList = idsStream.filter(this::isValidId).collect(Collectors.toList());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return bookIdsList;
    }

    public boolean isValidId(String bookId) {
        return bookId != null && bookId.length() == 12;
    }

    public void updateIndex() {
        if (loadedFromIndex)
            return;

        try (PrintWriter writer = new PrintWriter(getFileToLoad(true))) {
            for (String bookId : ExecutionContext.INSTANCE.getBookIds())
                writer.println(bookId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getLoadedFileName() {
        return INDEX_FILE_NAME;
    }
}