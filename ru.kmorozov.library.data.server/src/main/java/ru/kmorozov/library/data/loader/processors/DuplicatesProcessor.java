package ru.kmorozov.library.data.loader.processors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.DuplicatedBookDTO;
import ru.kmorozov.library.data.model.dto.results.BooksBySize;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.utils.BookUtils;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DuplicatesProcessor implements IProcessor {

    protected static final Logger logger = Logger.getLogger(DuplicatesProcessor.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    protected BooksRepository booksRepository;

    @Autowired @Lazy
    private OneDriveProvider api;

    public List<DuplicatedBookDTO> findDuplicates() {
        return this.getDuplicates().stream().map(this::createDuplicateDTO).collect(Collectors.toList());
    }

    @Override
    public void process() {
        DuplicatesProcessor.logger.info("Process duplicates started.");

        for (final BooksBySize duplicate : this.getDuplicates())
            switch (duplicate.getFormat()) {
                case DJVU:
                case PDF:
                    final List<Book> books = duplicate.getBookIds()
                            .stream()
                            .map(id -> this.booksRepository.findById(id))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    if (books.size() < 2)
                        continue;

                    final Book mainBook = books.get(0);

                    for (final Book book : books)
                        BookUtils.mergeCategories(book, mainBook);

                    this.booksRepository.save(mainBook);

                    for (final Book book : books)
                        if (book == mainBook)
                            continue;
                        else {
                            this.booksRepository.delete(book);
                            try {
                                final OneDriveItem bookItem = this.api.getItem(book.getBookInfo().getPath());
                                this.api.delete(bookItem);
                            } catch (final IOException e) {
                                DuplicatesProcessor.logger.error(String.format("Failed delete OneDriveItem for %s : %s", mainBook.getBookInfo().getFileName(), e.getMessage()));
                            }
                        }

                    DuplicatesProcessor.logger.info(String.format("Duplicates for %s processed.", mainBook.getBookInfo().getFileName()));

                    break;
                default:
            }

        DuplicatesProcessor.logger.info("Process duplicates finished.");
    }

    private List<BooksBySize> getDuplicates() {
        TypedAggregation<Book> booksAggregation = Aggregation.newAggregation(Book.class,
                Aggregation.group("bookInfo.size", "bookInfo.format")
                        .addToSet("bookId").as("bookIds")
                        .count().as("count"),
                Aggregation.match(Criteria.where("count").gt(1.0)),
                Aggregation.project("bookIds", "count", "size", "format"),
                Aggregation.skip(0L)
        ).withOptions(new AggregationOptions(true, false, new Document("batchSize", 1000.0)));

        AggregationResults<BooksBySize> results = this.mongoTemplate.aggregate(booksAggregation, BooksBySize.class);

        return results.getMappedResults();
    }

    private DuplicatedBookDTO createDuplicateDTO(BooksBySize book) {
        DuplicatedBookDTO dto = new DuplicatedBookDTO(book);
        dto.setBooks(book.getBookIds().stream().map(id -> new BookDTO(this.booksRepository.findById(id).get(), false)).collect(Collectors.toList()));

        return dto;
    }
}
