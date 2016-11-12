package ru.kmorozov.gbd.desktop.library;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by km on 12.11.2016.
 */
public class OptionsBasedProducer implements IBookListProducer {

    private List<String> bookIdsList;

    public OptionsBasedProducer() {
        String bookId = GBDOptions.getBookId();
        String bookDirName = GBDOptions.getOutputDir();

        if (bookId != null && bookId.length() > 0 && isValidId(bookId)) bookIdsList = Collections.singletonList(bookId);
        else if (bookDirName != null && bookDirName.length() > 0) {
            File booksDir = new File(bookDirName);
            if (booksDir.exists()) {
                bookIdsList = new ArrayList<>();
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
            }
        }

        if (bookIdsList == null || bookIdsList.size() == 0) throw new RuntimeException("No books to load!");
    }

    @Override
    public List<String> getBookIds() {
        return bookIdsList;
    }

    private boolean isValidId(String bookId) {
        return bookId != null && bookId.length() == 12;
    }
}
