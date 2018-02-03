package ru.kmorozov.library.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import base.BookInfo;

/**
 * Created by km on 21.12.2016.
 */
public interface GoogleBooksRepository extends MongoRepository<BookInfo, String> {

    BookInfo findByBookId(String bookId);
}
