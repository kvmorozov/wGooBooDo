package ru.kmorozov.library.data.loader.utils;

import org.springframework.data.repository.CrudRepository;
import ru.kmorozov.library.data.model.book.Book;

import java.util.*;

/**
 * Created by sbt-morozov-kv on 07.04.2017.
 */
public class ConsistencyUtils {

    private static final boolean DEDUPLCATION_ENABLED = false;

    public static Collection<Book> deduplicate(final Collection<Book> books, final CrudRepository booksRepository) {
        if (!DEDUPLCATION_ENABLED)
            return books;

        final Collection<String> uniquePaths;
        final Collection<Book> deduplicatedBooks;
        for (final Book book : books)
            if (uniquePaths.add(book.getBookInfo().getPath()))
                deduplicatedBooks.add(book);
            else
                booksRepository.delete(book);

        return deduplicatedBooks;
    }
}
