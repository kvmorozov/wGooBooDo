package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.loader.BookListLoader;
import ru.kmorozov.gbd.core.logic.context.ContextProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.output.DummyReceiver;
import ru.kmorozov.library.data.loader.processors.IProcessor;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.IdType;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.data.server.ServerGBDOptions;
import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;

import java.io.IOException;
import java.util.Optional;

import static ru.kmorozov.library.data.model.book.BookInfo.BookFormat.PDF;
import static ru.kmorozov.library.data.model.book.BookInfo.BookType.GOOGLE_BOOK;

@Component
public class GbdProcessor implements IProcessor {

    protected static final Logger logger = Logger.getLogger(GbdProcessor.class);

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

    @Override
    public void process() {
        logger.info("Process GBD started.");

        OneDriveItem gbdRoot = getGbdRoot();
        if (gbdRoot == null) {
            logger.error("GBD root not found, exiting.");
            return;
        }

        ExecutionContext.initContext(new DummyReceiver(), true);

        ctx.initContext(gbdRoot);
        if (!ctx.getBookIdsList().isEmpty())
            for (String bookId : ctx.getBookIdsList()) {
                BookInfo bookInfo = ctx.getBookInfo(bookId);
                if (bookInfo != null) {
                    OneDriveItem dirItem = ctx.getBookDir(bookId);
                    Storage storage = storageRepository.findByUrl(dirItem.getId());
                    Optional<Book> opBook = booksRepository.findAllByStorage(storage)
                            .stream().filter(book -> book.getBookInfo().getFormat() == PDF).findFirst();
                    if (opBook.isPresent()) {
                        Book book = opBook.get();
                        book.addBookId(IdType.GOOGLE_BOOK_ID, bookId);
                        book.getBookInfo().setBookType(GOOGLE_BOOK);

                        booksRepository.save(book);

                        ctx.updateBookInfo(bookInfo);

                        logger.info(String.format("BookId %s processed.", bookId));
                    }
                }
            }

        logger.info("Process GBD finished.");
    }

    private OneDriveItem getGbdRoot() {
        try {
            OneDriveItem[] searchResults = api.search(BookListLoader.INDEX_FILE_NAME);
            if (searchResults != null && searchResults.length == 1)
                return searchResults[0].getParent();
            else
                logger.error("Cannot find GBD root!");
        } catch (IOException e) {
            logger.error("Search error", e);
        }

        return null;
    }

    public void load() {
        GBDOptions.init(new ServerGBDOptions());

        ContextProvider.setDefaultContextProvider(dbCtx);
        ExecutionContext.initContext(new DummyReceiver(), 1 == producer.getBookIds().size());
        ExecutionContext.INSTANCE.addBookContext(producer, new DummyProgress(), new ServerPdfMaker());

        ExecutionContext.INSTANCE.execute();
    }
}
