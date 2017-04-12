package ru.kmorozov.library.data.repository;

import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */
public interface BooksRepository extends MongoRepository<Book, String> {

    List<Book> findAllBy(TextCriteria criteria);

    List<Book> findAllByStorage(Storage storage);

    List<Book> findAllByStorageAndBookInfoFormat(Storage storage, String format);

    Stream<Book> streamByBookInfoFormat (String format);

    Book findOneByBookInfoPath (String path);
}
