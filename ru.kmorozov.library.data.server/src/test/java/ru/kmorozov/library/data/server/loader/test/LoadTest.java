package ru.kmorozov.library.data.server.loader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by km on 26.12.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoConfiguration.class})
public class LoadTest {

    private static final String TEST_DIR = "J:\\_Книги";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Test
    public void categoryLoadTest() throws IOException {
        long categoryCount = categoryRepository.count();
        long storageCount = storageRepository.count();

        if (categoryCount > 0) {
            System.out.println("Categories loaded: " + categoryCount);
            categoryRepository.deleteAll();
        }

        if (storageCount > 0) {
            System.out.println("Storages loaded: " + storageCount);
            storageRepository.deleteAll();
        }

        Files.walk(Paths.get(TEST_DIR)).forEach(filePath -> {
            File file = filePath.toFile();
            if (file.isDirectory()) {
                Category category = new Category();
                category.setName(file.getName());

                categoryRepository.save(category);

                Storage storage = new Storage();
                storage.setStorageType(Storage.StorageType.LocalFileSystem);
                storage.setUrl(filePath.toString());
                storage.addCategory(category);

                storageRepository.save(storage);

                Storage parentStorage = storageRepository.findByUrl(filePath.getParent().toString());

                if (parentStorage != null) {
                    storage.setParent(parentStorage);
                    category.addParents(parentStorage.getCategories());
                }

                category.addStorage(storage);
                storageRepository.save(storage);
                categoryRepository.save(category);
            }
        });
    }
}
