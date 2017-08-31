package ru.kmorozov.library.data.loader.utils;

import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.converters.mongo.MongoConverterUtils;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.DuplicatedBookDTO;
import ru.kmorozov.library.data.model.dto.results.BooksBySize;
import ru.kmorozov.library.data.repository.BooksRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DuplicatesProcessor {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoConverterUtils converter;

    @Autowired
    protected BooksRepository booksRepository;

    public List<DuplicatedBookDTO> process() {
        TypedAggregation<Book> booksAggregation = Aggregation.newAggregation(Book.class,
                Aggregation.group("bookInfo.size", "bookInfo.format")
                        .addToSet("bookId").as("bookIds")
                        .count().as("count"),
                Aggregation.match(Criteria.where("count").gt(1.0)),
                Aggregation.project("bookIds", "count", "size", "format"),
                Aggregation.skip(0l)
        ).withOptions(new AggregationOptions(true, false, new BasicDBObject("batchSize", 1000.0)));

        AggregationResults<BooksBySize> results = mongoTemplate.aggregate(booksAggregation, BooksBySize.class);

        List<BooksBySize> booksBySizeList = converter.mapAggregationResults(BooksBySize.class, results.getRawResults(), "books");

        return booksBySizeList.stream().map(this::createDuplicateDTO).collect(Collectors.toList());
    }

    private DuplicatedBookDTO createDuplicateDTO(BooksBySize book) {
        DuplicatedBookDTO dto = new DuplicatedBookDTO(book);
        dto.setBooks(book.getBookIds().stream().map(id -> new BookDTO(booksRepository.findOne(id), false)).collect(Collectors.toList()));

        return dto;
    }
}
