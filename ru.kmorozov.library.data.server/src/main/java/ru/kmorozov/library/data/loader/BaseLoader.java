package ru.kmorozov.library.data.loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;

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
}
