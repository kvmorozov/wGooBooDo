package ru.kmorozov.library.data.loader.processors;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DuplicatesProcessor {

    protected static final Logger logger = Logger.getLogger(DuplicatesProcessor.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    protected BooksRepository booksRepository;

    @Autowired
    private OneDriveProvider api;

    public List<DuplicatedBookDTO> findDuplicates() {
        return getDuplicates().stream().map(this::createDuplicateDTO).collect(Collectors.toList());
    }

    public void processDuplicates() {
        logger.info("Process duplicates started.");

        for (BooksBySize duplicate : getDuplicates())
            switch (duplicate.getFormat()) {
                case DJVU:
                case PDF:
                    List<Book> books = duplicate.getBookIds()
                            .stream()
                            .map(id -> booksRepository.findById(id))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    if (books.size() < 2)
                        continue;

                    Book mainBook = books.get(0);

                    for (Book book : books)
                        BookUtils.mergeCategories(book, mainBook);

                    booksRepository.save(mainBook);

                    for (Book book : books)
                        if (book == mainBook)
                            continue;
                        else {
                            booksRepository.delete(book);
                            try {
                                OneDriveItem bookItem = api.getItem(book.getBookInfo().getPath());
                                api.delete(bookItem);
                            } catch (IOException e) {
                                logger.error(String.format("Failed delete OneDriveItem for %s : %s", mainBook.getBookInfo().getFileName(), e.getMessage()));
                            }
                        }

                    logger.info(String.format("Duplicates for %s processed.", mainBook.getBookInfo().getFileName()));

                    break;
                default:
            }

        logger.info("Process duplicates finished.");
    }

    private List<BooksBySize> getDuplicates() {
        final TypedAggregation<Book> booksAggregation = Aggregation.newAggregation(Book.class,
                Aggregation.group("bookInfo.size", "bookInfo.format")
                        .addToSet("bookId").as("bookIds")
                        .count().as("count"),
                Aggregation.match(Criteria.where("count").gt(1.0)),
                Aggregation.project("bookIds", "count", "size", "format"),
                Aggregation.skip(0l)
        ).withOptions(new AggregationOptions(true, false, new Document("batchSize", 1000.0)));

        final AggregationResults<BooksBySize> results = mongoTemplate.aggregate(booksAggregation, BooksBySize.class);

        return results.getMappedResults();
    }

    private DuplicatedBookDTO createDuplicateDTO(final BooksBySize book) {
        final DuplicatedBookDTO dto = new DuplicatedBookDTO(book);
        dto.setBooks(book.getBookIds().stream().map(id -> new BookDTO(booksRepository.findById(id).get(), false)).collect(Collectors.toList()));

        return dto;
    }
}
