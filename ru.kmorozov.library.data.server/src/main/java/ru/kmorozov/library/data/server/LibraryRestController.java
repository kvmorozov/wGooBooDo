package ru.kmorozov.library.data.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
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
import ru.kmorozov.library.data.model.IDataRestServer;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.dto.StorageDTO;
import ru.kmorozov.library.data.repository.GoogleBooksRepository;
import ru.kmorozov.library.data.repository.StorageRepository;

import java.util.List;
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

    private static transient BookContextLoader googleBooksLoader;

    @Override
    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }

    @Override
    @RequestMapping("/synchronizeGoogleBook")
    public boolean synchronizeGoogleBook(@RequestParam(name = "bookId") String bookId) {
        try {
            BookInfo existBookInfo = googleBooksRepository.findByBookId(bookId);
            if (existBookInfo == null) {
                if (googleBooksLoader == null) {
                    synchronized (LibraryRestController.class) {
                        if (googleBooksLoader == null) {
                            GBDOptions.init(new LocalSystemOptions());
                            ExecutionContext.initContext(new DummyBookInfoOutput(), false);
                            googleBooksLoader = new BookContextLoader();
                        }
                    }
                }

                BookInfo loadedBookInfo = googleBooksLoader.getBookInfo(bookId);

                if (loadedBookInfo != null) googleBooksRepository.save(loadedBookInfo);
            }

            logger.info("Synchronized Google book " + bookId);

            return true;
        } catch (Exception ex) {
            logger.info(String.format("Synchronization of Google book %s failed with %s", bookId, ex.getMessage()));
            return false;
        }
    }

    @Override
    @RequestMapping("/storagesByParentId")
    public List<StorageDTO> getStoragesByParentId(@RequestParam(name = "storageId") String storageId) {
        Storage parentStorage = StringUtils.isEmpty(storageId) ? null : storageRepository.findOne(storageId);
        return storageRepository.findAllByParent(parentStorage).stream().map(storage -> new StorageDTO(storage)).collect(Collectors.toList());
    }
}
