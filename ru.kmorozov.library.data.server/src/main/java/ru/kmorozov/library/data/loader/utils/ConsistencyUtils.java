package ru.kmorozov.library.data.loader.utils;

import org.springframework.data.repository.CrudRepository;
import ru.kmorozov.library.data.model.book.Book;

import java.util.*;

/**
 * Created by sbt-morozov-kv on 07.04.2017.
 */
public class ConsistencyUtils {

    private static final boolean DEDUPLCATION_ENABLED = false;

    public static Collection<Book> deduplicate(Collection<Book> books, CrudRepository booksRepository) {
        if (!ConsistencyUtils.DEDUPLCATION_ENABLED)
            return books;

        Collection<String> uniquePaths = new ArrayList<>();
        Collection<Book> deduplicatedBooks  = new ArrayList<>();
        for (Book book : books)
            if (uniquePaths.add(book.getBookInfo().getPath()))
                deduplicatedBooks.add(book);
            else
                booksRepository.delete(book);

        return deduplicatedBooks;
    }
}
