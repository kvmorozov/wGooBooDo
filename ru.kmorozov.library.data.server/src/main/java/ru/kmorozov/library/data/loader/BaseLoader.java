package ru.kmorozov.library.data.loader;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.LoaderExecutor.State;
import ru.kmorozov.library.data.loader.netty.EventSender;
import ru.kmorozov.library.data.model.book.*;
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.utils.BookUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public abstract class BaseLoader implements ILoader, Runnable {

    private static final Logger logger = Logger.getLogger(BaseLoader.class);
    protected Collection<Object> links = new ArrayList<>();
    protected volatile State state = State.STOPPED;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected StorageRepository storageRepository;

    @Autowired
    protected BooksRepository booksRepository;

    public void clear() {
        final long categoryCount = categoryRepository.count();
        final long storageCount = storageRepository.count();
        final long booksCount = booksRepository.count();

        if (0 < categoryCount) {
            logger.info("Categories loaded: " + categoryCount);
            categoryRepository.deleteAll();
        }

        if (0 < storageCount) {
            logger.info("Storages loaded: " + storageCount);
            storageRepository.deleteAll();
        }

        if (0 < booksCount) {
            logger.info("Books loaded: " + storageCount);
            booksRepository.deleteAll();
        }
    }

    private Category getOrCreateCategory(final String name) {
        Category category = categoryRepository.findOneByName(name);
        if (null == category) {
            category = new Category();
            category.setName(name);

            categoryRepository.save(category);
        }

        return category;
    }

    private Storage getOrCreateStorage(final ServerItem serverItem) {
        final Storage storage = storageRepository.findByUrl(serverItem.getUrl());
        return fillStorage(null == storage ? new Storage() : storage, serverItem);
    }

    protected static Storage fillStorage(final Storage storage, final ServerItem serverItem) {
        storage.setStorageType(serverItem.getStorageType());
        storage.setUrl(serverItem.getUrl());
        storage.setName(serverItem.getName());
        storage.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
        storage.setStorageInfo(new StorageInfo(serverItem.getFilesCount()));
        storage.getStorageInfo().setLastChecked(System.currentTimeMillis());

        return storage;
    }

    protected Category getCategoryByServerItem(final ServerItem serverItem) {
        final Category category = getOrCreateCategory(serverItem.getName());
        final Storage storage = getOrCreateStorage(serverItem);

        storage.addCategory(category);

        storageRepository.save(storage);

        final Storage parentStorage = null == serverItem.getParent() ? null : storageRepository.findByUrl(serverItem.getParent().getUrl());

        if (null != parentStorage) {
            storage.setParent(parentStorage);
            storageRepository.save(storage);

            category.addParents(parentStorage.getCategories());
            categoryRepository.save(category);
        }

        category.addStorage(storage);
        categoryRepository.save(category);

        return storage.getMainCategory();
    }

    protected abstract Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException;

    public abstract void processLinks();

    public abstract boolean postponedLinksLoad();

    protected void updateStorage(final Storage storage) throws IOException {
        final StorageInfo storageInfo = null == storage.getStorageInfo() ? new StorageInfo() : storage.getStorageInfo();

        getItemsStreamByStorage(storage)
                .filter(ServerItem::isLoadableOrLink)
                .forEach(serverItem -> {
                    if (!serverItem.isDirectory()) {
                        final BookFormat bookFormat = BookUtils.getFormat(serverItem.getName());
                        if (BookFormat.UNKNOWN != bookFormat) {
                            final Book existBook = booksRepository.findOneByBookInfoPath(serverItem.getUrl());
                            if (null == existBook) {
                                final Book book = new Book();

                                final BookInfo bookInfo = new BookInfo();
                                bookInfo.setFileName(serverItem.getName());
                                bookInfo.setPath(serverItem.getUrl());
                                bookInfo.setFormat(bookFormat);
                                bookInfo.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
                                bookInfo.setSize(serverItem.getSize());

                                book.setBookInfo(bookInfo);
                                book.setStorage(storage);

                                if (book.isLink() && !postponedLinksLoad()) {
                                    links.add(serverItem.getOriginalItem());
                                    EventSender.INSTANCE.sendInfo(logger, "Added link " + serverItem.getName());
                                } else {
                                    booksRepository.save(book);
                                    storage.getStorageInfo().incFilesCount();
                                    EventSender.INSTANCE.sendInfo(logger, "Added file " + serverItem.getName());
                                }
                            } else {
                                final Date oldDate = existBook.getBookInfo().getLastModifiedDateTime();
                                final Date newDate = serverItem.getLastModifiedDateTime();
                                final boolean dateCondition = null == oldDate || oldDate.before(newDate);

                                final long oldSize = existBook.getBookInfo().getSize();
                                final long newSize = serverItem.getSize();
                                final boolean sizeCondition = 0l == oldSize || oldSize != newSize;

                                boolean storageCondition = !existBook.getStorage().equals(storage);

                                if (dateCondition || sizeCondition || storageCondition) {
                                    existBook.getBookInfo().setFileName(serverItem.getName());
                                    existBook.getBookInfo().setLastModifiedDateTime(newDate);
                                    existBook.getBookInfo().setSize(newSize);
                                    existBook.setStorage(storage);
                                    booksRepository.save(existBook);

                                    EventSender.INSTANCE.sendInfo(logger, "Updated file " + serverItem.getName());
                                }
                            }
                        }
                    }
                });

        storageInfo.setLastChecked(System.currentTimeMillis());

        storage.setStorageInfo(storageInfo);
        storageRepository.save(storage);
    }

    @Override
    public void run() {
        try {
            load();
        } catch (IOException | UncheckedIOException e) {
            this.state = State.STOPPED;

            e.printStackTrace();
        }
    }

    void setState(final State state) {
        this.state = state;
    }

    State getState() {
        return state;
    }

    public boolean isStopped() {
        return LoaderExecutor.State.STOPPED == state;
    }

    public abstract Storage refresh(Storage storage);
}
