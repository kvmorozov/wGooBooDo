package ru.kmorozov.library.utils;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.Category;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 26.12.2016.
 */
public class BookUtils {

    private static final Logger logger = Logger.getLogger(BookUtils.class.getName());

    public static BookInfo.BookFormat getFormat(String fileName) {
        for (BookInfo.BookFormat format : BookInfo.BookFormat.values())
            if (fileName.endsWith(format.getExt()))
                return format;

        BookUtils.logger.log(Level.INFO, "Unknown format for file " + fileName);
        return BookInfo.BookFormat.UNKNOWN;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < (long) unit) return bytes + " B";
        int exp = (int) (Math.log((double) bytes) / Math.log((double) unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", (double) bytes / Math.pow((double) unit, (double) exp), pre);
    }

    public static void mergeCategories(final Book bookFrom, final Book bookTo) {
        if (bookFrom == bookTo)
            return;

        final Set<Category> inheritedFrom = bookFrom.getStorage().getCategories();
        final Set<Category> inheritedTo = bookTo.getStorage().getCategories();

        final Set<Category> ownFrom = bookFrom.getCategories() == null ? new HashSet<>() : bookFrom.getCategories();
        final Set<Category> ownTo = bookTo.getCategories() == null ? new HashSet<>() : bookTo.getCategories();

        final Set<Category> merged = new HashSet<>();
        final Set<Category> inheritedMerged = new HashSet<>();

        merged.addAll(ownTo);
        merged.addAll(ownFrom);

        inheritedMerged.addAll(inheritedFrom);
        inheritedMerged.removeAll(inheritedTo);

        merged.addAll(inheritedMerged);

        bookTo.setCategories(merged);
    }
}
