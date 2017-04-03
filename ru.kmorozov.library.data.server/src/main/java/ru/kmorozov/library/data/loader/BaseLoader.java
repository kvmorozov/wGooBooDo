package ru.kmorozov.library.data.loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.kmorozov.library.data.model.book.*;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.utils.BookUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public abstract class BaseLoader implements ILoader {

    private static final Logger logger = Logger.getLogger(BaseLoader.class);
    protected List<Object> links = new ArrayList<>();

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

    private Category getorCreatecategory(String name) {
        Category category = categoryRepository.findOneByName(name);
        if (category == null) {
            category = new Category();
            category.setName(name);

            categoryRepository.save(category);
        }

        return category;
    }

    protected Category getCategoryByServerItem(ServerItem serverItem) {
        Category category;
        Storage storage = storageRepository.findByUrl(serverItem.getUrl());

        if (storage == null || storage.getCategories().size() < 1) {
            category = getorCreatecategory(serverItem.getName());

            storage = new Storage();
            storage.setStorageType(serverItem.getStorageType());
            storage.setUrl(serverItem.getUrl());
            storage.addCategory(category);
            storage.setName(serverItem.getName());
            storage.setLastModifiedDateTime(serverItem.getLastModifiedDateTime());
            storage.setStorageInfo(new StorageInfo(serverItem.getFilesCount()));

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

            return category;
        } else
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
                            Book book = new Book();

                            BookInfo bookInfo = new BookInfo();
                            bookInfo.setFileName(serverItem.getName());
                            bookInfo.setPath(serverItem.getUrl());
                            bookInfo.setFormat(bookFormat);

                            book.setBookInfo(bookInfo);
                            book.setStorage(storage);

                            if (bookInfo.isLink() && !postponedLinksLoad())
                                links.add(serverItem.getOriginalItem());
                            else {
                                booksRepository.save(book);
                                storage.getStorageInfo().incFilesCount();
                            }
                        }
                    }
                });

        storageInfo.setLastChecked(System.currentTimeMillis());

        storage.setStorageInfo(storageInfo);
        storageRepository.save(storage);
    }
}
