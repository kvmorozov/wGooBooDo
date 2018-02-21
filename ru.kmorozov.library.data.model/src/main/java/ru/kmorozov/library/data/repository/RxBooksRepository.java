package ru.kmorozov.library.data.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.kmorozov.library.data.model.book.Book;

public interface RxBooksRepository extends ReactiveMongoRepository<Book, String> {

    @Query("{ 'bookInfo.fileName' : { $regex: ?0 } }")
    Flux<Book> findBooksByRegexBookInfoFileName(String regexp);
}
