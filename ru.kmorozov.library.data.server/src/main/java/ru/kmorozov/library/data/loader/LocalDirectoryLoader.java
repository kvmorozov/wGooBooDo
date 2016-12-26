package ru.kmorozov.library.data.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kmorozov.library.data.model.book.Category;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.CategoryRepository;
import ru.kmorozov.library.data.repository.StorageRepository;

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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StorageRepository storageRepository;

    private Path basePath;

    public LocalDirectoryLoader(@Autowired String localBasePath) {
        this.basePath = Paths.get(localBasePath);
    }

    public void clear() {
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
    }

    public void load() throws IOException {
        Files.walk(basePath).forEach(filePath -> {
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
                    storageRepository.save(storage);

                    category.addParents(parentStorage.getCategories());
                }

                category.addStorage(storage);
                categoryRepository.save(category);
            }
        });
    }
}
