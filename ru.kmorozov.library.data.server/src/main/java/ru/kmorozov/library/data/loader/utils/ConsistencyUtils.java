package ru.kmorozov.library.data.loader.utils;

import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.repository.BooksRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sbt-morozov-kv on 07.04.2017.
 */
public class ConsistencyUtils {

    private static final boolean DEDUPLCATION_ENABLED = false;

    public static List<Book> deduplicate(List<Book> books, BooksRepository booksRepository) {
        if (!DEDUPLCATION_ENABLED)
            return books;

        Set<String> uniquePaths = new HashSet<>();
        List<Book> deduplicatedBooks = new ArrayList<>();
        for (Book book : books)
            if (uniquePaths.add(book.getBookInfo().getPath()))
                deduplicatedBooks.add(book);
            else
                booksRepository.delete(book);

        return deduplicatedBooks;
    }
}
