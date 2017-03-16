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
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
public abstract class BaseLoader implements ILoader {

    private static final Logger logger = Logger.getLogger(BaseLoader.class);

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

    protected Category getCategoryByServerItem(ServerItem serverItem) {
        Category category;
        Storage storage = storageRepository.findByUrl(serverItem.getUrl());

        if (storage == null || storage.getCategories().size() < 1) {
            category = new Category();
            category.setName(serverItem.getName());

            categoryRepository.save(category);

            storage = new Storage();
            storage.setStorageType(Storage.StorageType.LocalFileSystem);
            storage.setUrl(serverItem.getUrl());
            storage.addCategory(category);

            storageRepository.save(storage);

            Storage parentStorage = storageRepository.findByUrl(serverItem.getParent().getUrl());

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

    protected void updateStorage(Storage storage) throws IOException {
        if (storage.getStorageType() != Storage.StorageType.LocalFileSystem)
            return;

        StorageInfo storageInfo = storage.getStorageInfo() == null ? new StorageInfo() : storage.getStorageInfo();
//        List<Book> books = booksRepository.findAllByStorage(storage);
//        Map<String, Book> oldBooksMap = books.stream().collect(Collectors.toMap(Book::getBookKey, Function.identity()));

        int counter = 0;

        getItemsStreamByStorage(storage).forEach(serverItem -> {
            if (!serverItem.isDirectory()) {
                BookInfo.BookFormat bookFormat = BookUtils.getFormat(serverItem.getName());
                if (bookFormat != BookInfo.BookFormat.UNKNOWN) {
                    Book book = new Book();

                    BookInfo bookInfo = new BookInfo();
                    bookInfo.setFileName(serverItem.getUrl());
                    bookInfo.setFormat(bookFormat);

                    book.setBookInfo(bookInfo);
                    book.setStorage(storage);

                    booksRepository.save(book);
                }
            }
        });

        storageInfo.setLastChecked(System.currentTimeMillis());

        storage.setStorageInfo(storageInfo);
        storageRepository.save(storage);
    }
}
