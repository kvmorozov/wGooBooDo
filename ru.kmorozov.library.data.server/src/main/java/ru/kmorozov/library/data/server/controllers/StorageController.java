package ru.kmorozov.library.data.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.impl.LoaderExecutor;
import ru.kmorozov.library.data.loader.processors.DuplicatesProcessor;
import ru.kmorozov.library.data.loader.processors.JstorProcessor;
import ru.kmorozov.library.data.loader.processors.gbd.GbdRemoteProcessor;
import ru.kmorozov.library.data.loader.utils.BookUtils;
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.dto.BookDTO;
import ru.kmorozov.library.data.model.dto.DuplicatedBookDTO;
import ru.kmorozov.library.data.model.dto.ItemDTO;
import ru.kmorozov.library.data.model.dto.StorageDTO;
import ru.kmorozov.library.data.model.dto.UserDTO;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@EnableMongoRepositories(basePackages = "ru.kmorozov.library.data.repository")
@ComponentScan(basePackageClasses = {LoaderConfiguration.class, LoaderExecutor.class, DuplicatesProcessor.class, GbdRemoteProcessor.class, LoaderExecutor.class, JstorProcessor.class})
@Conditional(StorageEnabledCondition.class)
public class StorageController implements IDataRestServer {

    protected static final Logger logger = Logger.getLogger(StorageController.class);

    @Autowired
    @Lazy
    private StorageRepository storageRepository;

    @Autowired
    @Lazy
    private BooksRepository booksRepository;

    @Autowired
    @Lazy
    private LoaderExecutor loader;

    @Autowired
    @Lazy
    private DuplicatesProcessor duplicatesProcessor;

    @Autowired
    @Lazy
    private JstorProcessor jstorProcessor;

    @Autowired
    @Lazy
    private GbdRemoteProcessor gbdProcessor;

    @Override
    @RequestMapping("/login")
    public UserDTO login(@RequestParam(name = "login") final String login) {
        return new UserDTO(login);
    }

    @RequestMapping("/jstorUpdate")
    public void jstorUpdate() {
        jstorProcessor.process();
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
    public ItemDTO getItem(@RequestParam(name = "itemId") final String itemId, @RequestParam(name = "itemType") final ItemDTO.ItemType itemType, @RequestParam(name = "refresh") final boolean refresh) {
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
        final LoaderExecutor.State state = LoaderExecutor.State.valueOf(stateString);

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
        return duplicatesProcessor.findDuplicates();
    }

    @Override
    @RequestMapping("/synchronizeDb")
    public void synchronizeDb() {
        loader.start();
    }

    @RequestMapping("/processDuplicates")
    public void processDuplicates() {
        duplicatesProcessor.process();
    }

    @RequestMapping("/gbdLoadRemote")
    public void gbdLoad(@RequestParam(name = "bookId", required = false) final String bookId) {
        gbdProcessor.load(bookId);
    }

}
