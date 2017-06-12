package ru.kmorozov.library.data.loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.kmorozov.library.data.loader.netty.EventSender;
import ru.kmorozov.library.data.model.book.*;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.utils.BookUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public abstract class BaseLoader implements ILoader, Runnable {

    private static final Logger logger = Logger.getLogger(BaseLoader.class);
    protected List<Object> links = new ArrayList<>();
    protected volatile LoaderExecutor.State state = LoaderExecutor.State.STOPPED;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected StorageRepository storageRepository;

    @Autowired
    protected BooksRepository booksRepository;

    public void clear() {
        long categoryCount = categoryRepository.count();
        long storageCount = storageRepository.count();
        long booksCount = booksRepository.count();

        if (categoryCount > 0) {
            logger.log(Level.INFO, "Categories loaded: " + categoryCount);
            categoryRepository.deleteAll();
        }

        if (storageCount > 0) {
            logger.log(Level.INFO, "Storages loaded: " + storageCount);
            storageRepository.deleteAll();
        }

        if (booksCount > 0) {
            logger.log(Level.INFO, "Books loaded: " + storageCount);
            booksRepository.deleteAll();
        }
    }

    private Category getOrCreateCategory(String name) {
        Category category = categoryRepository.findOneByName(name);
        if (category == null) {
            category = new Category();
            category.setName(name);

            categoryRepository.save(category);
        }

        return category;
    }

    private Storage getOrCreateStorage(ServerItem serverItem) {
        Storage storage = storageRepository.findByUrl(serverItem.getUrl());
        return fillStorage(storage == null ? new Storage() : storage, serverItem);
    }

    protected Storage fillStorage(Storage storage, ServerItem serverItem) {
        storage.setStorageType(serverItem.getStorageType());
        storage.setUrl(serverItem.getUrl());
        storage.setName(serverItem.getName());
        storage.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
        storage.setStorageInfo(new StorageInfo(serverItem.getFilesCount()));
        storage.getStorageInfo().setLastChecked(System.currentTimeMillis());

        return storage;
    }

    protected Category getCategoryByServerItem(ServerItem serverItem) {
        Category category = getOrCreateCategory(serverItem.getName());
        Storage storage = getOrCreateStorage(serverItem);

        storage.addCategory(category);

        storageRepository.save(storage);

        Storage parentStorage = serverItem.getParent() == null ? null : storageRepository.findByUrl(serverItem.getParent().getUrl());

        if (parentStorage != null) {
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

    public abstract void processLinks() throws IOException;

    public abstract boolean postponedLinksLoad();

    protected void updateStorage(Storage storage) throws IOException {
        StorageInfo storageInfo = storage.getStorageInfo() == null ? new StorageInfo() : storage.getStorageInfo();

        getItemsStreamByStorage(storage)
                .filter(ServerItem::isLoadableOrLink)
                .forEach(serverItem -> {
                    if (!serverItem.isDirectory()) {
                        BookInfo.BookFormat bookFormat = BookUtils.getFormat(serverItem.getName());
                        if (bookFormat != BookInfo.BookFormat.UNKNOWN) {
                            Book existBook = booksRepository.findOneByBookInfoPath(serverItem.getUrl());
                            if (existBook == null) {
                                Book book = new Book();

                                BookInfo bookInfo = new BookInfo();
                                bookInfo.setFileName(serverItem.getName());
                                bookInfo.setPath(serverItem.getUrl());
                                bookInfo.setFormat(bookFormat);
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
                                Date oldDate = existBook.getBookInfo().getLastModifiedDateTime();
                                Date newDate = serverItem.getLastModifiedDateTime();
                                boolean dateCondition = oldDate == null || oldDate.before(newDate);

                                long oldSize = existBook.getBookInfo().getSize();
                                long newSize = serverItem.getSize();
                                boolean sizeCondition = oldSize == 0l || oldSize != newSize;

                                if (dateCondition || sizeCondition) {
                                    existBook.getBookInfo().setFileName(serverItem.getName());
                                    existBook.getBookInfo().setLastModifiedDateTime(newDate);
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
            setState(LoaderExecutor.State.STOPPED);

            e.printStackTrace();
        }
    }

    void setState(LoaderExecutor.State state) {
        this.state = state;
    }

    LoaderExecutor.State getState() {
        return state;
    }

    public boolean isStopped() {
        return state == LoaderExecutor.State.STOPPED;
    }

    public abstract Storage refresh(Storage storage);
}
