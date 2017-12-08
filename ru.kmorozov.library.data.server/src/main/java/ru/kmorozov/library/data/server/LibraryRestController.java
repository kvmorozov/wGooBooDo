package ru.kmorozov.library.data.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kmorozov.gbd.client.IRestClient;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.LocalSystemOptions;
import ru.kmorozov.gbd.core.config.storage.BookContextLoader;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.library.data.loader.LoaderExecutor;
import ru.kmorozov.library.data.loader.LoaderExecutor.State;
import ru.kmorozov.library.data.loader.utils.BookUtils;
import ru.kmorozov.library.data.loader.utils.DuplicatesProcessor;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.dto.*;
import ru.kmorozov.library.data.model.dto.ItemDTO.ItemType;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.GoogleBooksRepository;
import ru.kmorozov.library.data.repository.StorageRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by km on 19.12.2016.
 */

@RestController
public class LibraryRestController implements IRestClient, IDataRestServer {

    protected static final Logger logger = Logger.getLogger(HttpConnector.class);

    @Autowired
    private GoogleBooksRepository googleBooksRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private LoaderExecutor loader;

    @Autowired
    private DuplicatesProcessor duplicatesProcessor;

    private static transient BookContextLoader googleBooksLoader;

    @Override
    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }

    @Override
    @RequestMapping("/synchronizeGoogleBook")
    public boolean synchronizeGoogleBook(@RequestParam(name = "bookId") final String bookId) {
        try {
            final BookInfo existBookInfo = googleBooksRepository.findByBookId(bookId);
            if (null == existBookInfo) {
                if (null == googleBooksLoader) {
                    synchronized (LibraryRestController.class) {
                        if (null == googleBooksLoader) {
                            GBDOptions.init(new LocalSystemOptions());
                            ExecutionContext.initContext(new DummyBookInfoOutput(), false);
                            googleBooksLoader = new BookContextLoader();
                        }
                    }
                }

                final BookInfo loadedBookInfo = googleBooksLoader.getBookInfo(bookId);

                if (null != loadedBookInfo) googleBooksRepository.save(loadedBookInfo);
            }

            logger.info("Synchronized Google book " + bookId);

            return true;
        } catch (final Exception ex) {
            logger.info(String.format("Synchronization of Google book %s failed with %s", bookId, ex.getMessage()));
            return false;
        }
    }

    @Override
    @RequestMapping("/login")
    public UserDTO login(@RequestParam(name = "login") final String login) {
        return new UserDTO(login);
    }

    @Override
    @RequestMapping("/storagesByParentId")
    public List<StorageDTO> getStoragesByParentId(@RequestParam(name = "storageId") final String storageId) {
        final Storage parentStorage = StringUtils.isEmpty(storageId) ? null : storageRepository.findById(storageId).get();

        final List<Storage> realStorages = storageRepository.findAllByParent(parentStorage);
        final List<Book> linksInStorages = booksRepository.findAllByStorageAndBookInfoFormat(parentStorage, "LNK");
        linksInStorages.forEach(book -> loader.resolveLink(book));
        final List<Storage> linkedStorages = linksInStorages.stream()
                .filter(lnk -> null != lnk.getLinkInfo() && null != lnk.getLinkInfo().getLinkedStorage())
                .map(lnk -> lnk.getLinkInfo().getLinkedStorage())
                .collect(Collectors.toList());

        realStorages.addAll(linkedStorages);

        return realStorages.stream().map(StorageDTO::new).collect(Collectors.toList());
    }

    @Override
    @RequestMapping("/booksByStorageId")
    public List<BookDTO> getBooksByStorageId(@RequestParam(name = "storageId") final String storageId) {
        final Storage storage = StringUtils.isEmpty(storageId) ? null : storageRepository.findById(storageId).get();
        if (null == storage)
            return Collections.emptyList();

        return booksRepository.findAllByStorage(storage).stream()
                .filter(book -> !book.isBrokenLink())
                .map(book -> book.isLink() ? book.getLinkInfo().getLinkedBook() : book)
                .filter(Objects::nonNull)
                .map(BookUtils::createBookDIO)
                .collect(Collectors.toList());
    }

    @Override
    @RequestMapping("/itemsByStorageId")
    public List<ItemDTO> getItemsByStorageId(final String storageId) {
        final List<ItemDTO> result = getBooksByStorageId(storageId).stream().map(ItemDTO::new).collect(Collectors.toList());
        result.addAll(getStoragesByParentId(storageId).stream().map(ItemDTO::new).collect(Collectors.toList()));

        return result;
    }

    @Override
    @RequestMapping("/item")
    public ItemDTO getItem(@RequestParam(name = "itemId") final String itemId, @RequestParam(name = "itemType") final ItemType itemType, @RequestParam(name = "refresh") final boolean refresh) {
        switch (itemType) {
            case book:
                return new ItemDTO(new BookDTO(booksRepository.findById(itemId).get(), true));
            case storage:
                Storage storage = storageRepository.findById(itemId).get();
                if (refresh)
                    storage = loader.refresh(storage);
                final ItemDTO item = new ItemDTO(new StorageDTO(storage, true));
                if (refresh)
                    item.setUpdated();

                return item;
            default:
                return null;
        }
    }

    @Override
    @RequestMapping(value = "/updateLibrary", method = RequestMethod.POST)
    public void updateLibrary(@RequestParam(name = "state") final String stateString) {
        final State state = State.valueOf(stateString);

        switch (state) {
            case STARTED:
                loader.start();
                break;
            case PAUSED:
                loader.pause();
                break;
            case STOPPED:
                loader.stop();
        }
    }

    @Override
    @RequestMapping("/downloadBook")
    public BookDTO downloadBook(@RequestParam(name = "bookId") final String bookId) {
        final Book book = booksRepository.findById(bookId).get();
        if (BookUtils.bookLoaded(book))
            return new BookDTO(book, true);
        else {
            book.getStorage().setLocalPath(null);
            booksRepository.save(book);
        }

        loader.downloadBook(book);
        return new BookDTO(book, true);
    }

    @Override
    @RequestMapping("/findDuplicates")
    public List<DuplicatedBookDTO> findDuplicates() {
        return duplicatesProcessor.process();
    }
}
