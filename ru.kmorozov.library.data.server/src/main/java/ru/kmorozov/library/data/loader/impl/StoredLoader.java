package ru.kmorozov.library.data.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.library.data.loader.netty.EventSender;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.model.book.StorageInfo;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.utils.BookUtils;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Stream;

public abstract class StoredLoader extends BaseLoader{

    private static final Logger logger = Logger.getLogger(StoredLoader.class);

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected StorageRepository storageRepository;

    @Autowired
    protected BooksRepository booksRepository;

    public void clear() {
        long categoryCount = this.categoryRepository.count();
        long storageCount = this.storageRepository.count();
        long booksCount = this.booksRepository.count();

        if (0L < categoryCount) {
            StoredLoader.logger.info("Categories loaded: " + categoryCount);
            this.categoryRepository.deleteAll();
        }

        if (0L < storageCount) {
            StoredLoader.logger.info("Storages loaded: " + storageCount);
            this.storageRepository.deleteAll();
        }

        if (0L < booksCount) {
            StoredLoader.logger.info("Books loaded: " + storageCount);
            this.booksRepository.deleteAll();
        }
    }

    private Category getOrCreateCategory(String name) {
        Category category = this.categoryRepository.findOneByName(name);
        if (null == category) {
            category = new Category();
            category.setName(name);

            this.categoryRepository.save(category);
        }

        return category;
    }

    private Storage getOrCreateStorage(ServerItem serverItem) {
        Storage storage = this.storageRepository.findByUrl(serverItem.getUrl());
        return StoredLoader.fillStorage(null == storage ? new Storage() : storage, serverItem);
    }

    protected static Storage fillStorage(Storage storage, ServerItem serverItem) {
        storage.setStorageType(serverItem.getStorageType());
        storage.setUrl(serverItem.getUrl());
        storage.setName(serverItem.getName());
        storage.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
        storage.setStorageInfo(new StorageInfo(serverItem.getFilesCount()));
        storage.getStorageInfo().setLastChecked(System.currentTimeMillis());

        return storage;
    }

    protected Category getCategoryByServerItem(ServerItem serverItem) {
        Category category = this.getOrCreateCategory(serverItem.getName());
        Storage storage = this.getOrCreateStorage(serverItem);

        storage.addCategory(category);

        this.storageRepository.save(storage);

        Storage parentStorage = null == serverItem.getParent() ? null : this.storageRepository.findByUrl(serverItem.getParent().getUrl());

        if (null != parentStorage) {
            storage.setParent(parentStorage);
            this.storageRepository.save(storage);

            category.addParents(parentStorage.getCategories());
            this.categoryRepository.save(category);
        }

        category.addStorage(storage);
        this.categoryRepository.save(category);

        return storage.getMainCategory();
    }

    protected abstract Stream<ServerItem> getItemsStreamByStorage(Storage storage) throws IOException;

    public abstract void processLinks();

    public abstract boolean postponedLinksLoad();

    protected void updateStorage(Storage storage) throws IOException {
        StorageInfo storageInfo = null == storage.getStorageInfo() ? new StorageInfo() : storage.getStorageInfo();

        this.getItemsStreamByStorage(storage)
                .filter(ServerItem::isLoadableOrLink)
                .forEach(serverItem -> {
                    if (!serverItem.isDirectory()) {
                        BookFormat bookFormat = BookUtils.getFormat(serverItem.getName());
                        if (BookFormat.UNKNOWN != bookFormat) {
                            Book existBook = this.booksRepository.findOneByBookInfoPath(serverItem.getUrl());
                            if (null == existBook) {
                                Book book = new Book();

                                BookInfo bookInfo = new BookInfo();
                                bookInfo.setFileName(serverItem.getName());
                                bookInfo.setPath(serverItem.getUrl());
                                bookInfo.setFormat(bookFormat);
                                bookInfo.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
                                bookInfo.setSize(serverItem.getSize());

                                book.setBookInfo(bookInfo);
                                book.setStorage(storage);

                                if (book.isLink() && !this.postponedLinksLoad()) {
                                    this.links.add(serverItem.getOriginalItem());
                                    EventSender.INSTANCE.sendInfo(StoredLoader.logger, "Added link " + serverItem.getName());
                                } else {
                                    this.booksRepository.save(book);
                                    storage.getStorageInfo().incFilesCount();
                                    EventSender.INSTANCE.sendInfo(StoredLoader.logger, "Added file " + serverItem.getName());
                                }
                            } else {
                                Date oldDate = existBook.getBookInfo().getLastModifiedDateTime();
                                Date newDate = serverItem.getLastModifiedDateTime();
                                boolean dateCondition = null == oldDate || oldDate.before(newDate);

                                long oldSize = existBook.getBookInfo().getSize();
                                long newSize = serverItem.getSize();
                                boolean sizeCondition = 0L == oldSize || oldSize != newSize;

                                final boolean storageCondition = !existBook.getStorage().equals(storage);
                                final boolean nameCondition = serverItem.getName().equals(existBook.getBookInfo().getFileName());

                                if (dateCondition || sizeCondition || storageCondition || nameCondition) {
                                    existBook.getBookInfo().setFileName(serverItem.getName());
                                    existBook.getBookInfo().setLastModifiedDateTime(newDate);
                                    existBook.getBookInfo().setSize(newSize);
                                    existBook.setStorage(storage);
                                    this.booksRepository.save(existBook);

                                    EventSender.INSTANCE.sendInfo(StoredLoader.logger, "Updated file " + serverItem.getName());
                                }
                            }
                        }
                    }
                });

        storageInfo.setLastChecked(System.currentTimeMillis());

        storage.setStorageInfo(storageInfo);
        this.storageRepository.save(storage);
    }
}
