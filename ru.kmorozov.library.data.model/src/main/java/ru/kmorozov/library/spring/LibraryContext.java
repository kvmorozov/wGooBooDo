package ru.kmorozov.library.spring;

import org.springframework.context.support.StaticApplicationContext;
import ru.kmorozov.library.data.repository.BooksRepository;

/**
 * Created by sbt-morozov-kv on 15.12.2016.
 */
public final class LibraryContext extends StaticApplicationContext {

    public static final LibraryContext LIBRARY_CONTEXT = new LibraryContext();

    private LibraryContext() {
        this.registerSingleton("booksRepository", BooksRepository.class);
    }
}
