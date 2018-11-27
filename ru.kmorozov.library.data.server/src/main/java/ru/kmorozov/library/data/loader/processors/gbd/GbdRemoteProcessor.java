package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.ContextProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.output.DummyReceiver;
import ru.kmorozov.library.data.loader.processors.IGbdProcessor;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.IdType;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.data.server.options.ServerGBDOptions;
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.Optional;

import static ru.kmorozov.library.data.model.book.BookInfo.BookFormat.PDF;
import static ru.kmorozov.library.data.model.book.BookInfo.BookType.GOOGLE_BOOK;

@Component
@ComponentScan(basePackageClasses = {OneDriveContextLoader.class, ServerProducer.class, DbContextLoader.class, ServerGBDOptions.class})
@Conditional(StorageEnabledCondition.class)
public class GbdRemoteProcessor implements IGbdProcessor {

    protected static final Logger logger = Logger.getLogger(GbdRemoteProcessor.class);

    @Autowired
    @Lazy
    private OneDriveProvider api;

    @Autowired
    @Lazy
    private StorageRepository storageRepository;

    @Autowired
    @Lazy
    private BooksRepository booksRepository;

    @Autowired
    @Lazy
    private OneDriveContextLoader ctx;

    @Autowired
    @Lazy
    private ServerProducer producer;

    @Autowired
    @Lazy
    private DbContextLoader dbCtx;

    @Autowired
    @Qualifier("remote")
    private ServerGBDOptions options;

    @Override
    public void process() {
        GbdRemoteProcessor.logger.info("Process GBD started.");

        final OneDriveItem gbdRoot = this.getGbdRoot();
        if (gbdRoot == null) {
            GbdRemoteProcessor.logger.error("GBD root not found, exiting.");
            return;
        }

        ExecutionContext.initContext(new DummyReceiver(), true);

        this.ctx.initContext(gbdRoot);
        if (!this.ctx.getBookIdsList().isEmpty())
            for (final String bookId : this.ctx.getBookIdsList()) {
                final BookInfo bookInfo = this.ctx.getBookInfo(bookId);
                if (bookInfo != null) {
                    final OneDriveItem dirItem = this.ctx.getBookDir(bookId);
                    final Storage storage = this.storageRepository.findByUrl(dirItem.getId());
                    final Optional<Book> opBook = this.booksRepository.findAllByStorage(storage)
                            .stream().filter(book -> book.getBookInfo().getFormat() == PDF).findFirst();
                    if (opBook.isPresent()) {
                        final Book book = opBook.get();
                        book.addBookId(IdType.GOOGLE_BOOK_ID, bookId);
                        book.getBookInfo().setBookType(GOOGLE_BOOK);

                        this.booksRepository.save(book);

                        this.ctx.updateBookInfo(bookInfo);

                        GbdRemoteProcessor.logger.info(String.format("BookId %s processed.", bookId));
                    }
                }
            }

        GbdRemoteProcessor.logger.info("Process GBD finished.");
    }

    @Bean @Lazy
    public OneDriveItem getGbdRoot() {
        try {
            final OneDriveItem[] searchResults = this.api.search("books.ctx");
            if (searchResults != null && searchResults.length == 1)
                return searchResults[0].getParent();
            else
                GbdRemoteProcessor.logger.error("Cannot find GBD root!");
        } catch (final IOException e) {
            GbdRemoteProcessor.logger.error("Search error", e);
        }

        return null;
    }

    @Override
    public void load(final String bookId) {
        this.options.setBookId(bookId);
        GBDOptions.init(this.options);

        ContextProvider.setDefaultContextProvider(this.dbCtx);

        ExecutionContext.initContext(new DummyReceiver(), 1 == this.producer.getBookIds().size());
        ExecutionContext.INSTANCE.addBookContext(this.producer, new DummyProgress(), new ServerPdfMaker());

        ExecutionContext.INSTANCE.execute();
    }
}
