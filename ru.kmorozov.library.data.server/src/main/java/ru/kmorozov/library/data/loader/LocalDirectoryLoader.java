package ru.kmorozov.library.data.loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.model.book.*;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;
import ru.kmorozov.library.utils.BookUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by km on 26.12.2016.
 */

@Component
public class LocalDirectoryLoader {

    private static final Logger logger = Logger.getLogger(LocalDirectoryLoader.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private BooksRepository booksRepository;

    private Path basePath;

    public LocalDirectoryLoader(@Autowired String localBasePath) {
        this.basePath = Paths.get(localBasePath);
    }

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

    public void load() throws IOException {
        Files.walk(basePath).forEach(filePath -> {
            File file = filePath.toFile();
            if (file.isDirectory()) {
                Category category = getCategoryByPath(filePath);
                for (Storage storage : category.getStorages())
                    try {
                        updateStorage(storage);
                    } catch (IOException e) {
                        logger.log(Level.ERROR, "Error when updating storage: " + e.getMessage());
                    }
            }
        });
    }

    private Category getCategoryByPath(Path filePath) {
        Category category;
        Storage storage = storageRepository.findByUrl(filePath.toString());

        if (storage == null || storage.getCategories().size() < 1) {
            category = new Category();
            category.setName(filePath.toFile().getName());

            categoryRepository.save(category);

            storage = new Storage();
            storage.setStorageType(Storage.StorageType.LocalFileSystem);
            storage.setUrl(filePath.toString());
            storage.addCategory(category);

            storageRepository.save(storage);

            Storage parentStorage = storageRepository.findByUrl(filePath.getParent().toString());

            if (parentStorage != null) {
                storage.setParent(parentStorage);
                storageRepository.save(storage);

                category.addParents(parentStorage.getCategories());
            }

            category.addStorage(storage);
            categoryRepository.save(category);

            return category;
        }
        else
            return storage.getCategories().get(0);
    }

    private void updateStorage(Storage storage) throws IOException {
        if (storage.getStorageType() != Storage.StorageType.LocalFileSystem)
            return;

        StorageInfo storageInfo = storage.getStorageInfo() == null ? new StorageInfo() : storage.getStorageInfo();
//        List<Book> books = booksRepository.findAllByStorage(storage);
//        Map<String, Book> oldBooksMap = books.stream().collect(Collectors.toMap(Book::getBookKey, Function.identity()));

        Path storagePath = Paths.get(storage.getUrl());
        int counter = 0;

        Files.walk(storagePath, 1).forEach(filePath -> {
            if (!filePath.toFile().isDirectory()) {
                BookInfo.BookFormat bookFormat = BookUtils.getFormat(filePath);
                if (bookFormat != BookInfo.BookFormat.UNKNOWN) {
                    Book book = new Book();

                    BookInfo bookInfo = new BookInfo();
                    bookInfo.setFileName(filePath.toString());
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