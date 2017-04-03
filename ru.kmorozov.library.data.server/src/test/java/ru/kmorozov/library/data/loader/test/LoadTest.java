package ru.kmorozov.library.data.loader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.LocalDirectoryLoader;
import ru.kmorozov.library.data.loader.OneDriveLoader;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.StorageRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Created by km on 26.12.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LoaderConfiguration.class})
public class LoadTest {

    private static final String MULTIPLE_LNK_DIR = "J:\\OneDrive\\_Книги\\Религиозные вопросы\\Христианство";
    private static final String delimiter = Pattern.quote(File.separator);

    @Autowired
    private LocalDirectoryLoader fsLoader;

    @Autowired
    private OneDriveLoader oneLoader;

    @Autowired
    protected StorageRepository storageRepository;

    @Test
    public void categoryLoadTestFs() throws IOException {
//        fsLoader.clear();
        fsLoader.load();
        fsLoader.processLinks();
    }

    @Test
    public void categoryLoadTestOne() throws IOException {
        oneLoader.clear();
        oneLoader.load();
    }

    @Test
    public void loadLinksTestOne() throws IOException {
        oneLoader.processLinks();
    }

    @Test
    public void loadLinksTestOneMiltiDir() throws IOException {
        String[] names = MULTIPLE_LNK_DIR.split(delimiter);
        List<Storage> storages = storageRepository.findAllByName(names[names.length - 1]);
        String parentName = null;

        for (int index = names.length - 1; index > 0; index--) {
            if (storages.size() == 1)
                break;
            else {
                parentName = names[index - 1];
                if (parentName != null) {
                    List<Storage> filteredStorages = new ArrayList<>();
                    for (Storage storage : storages)
                        if (storage.getParent().getName().equals(parentName))
                            filteredStorages.add(storage);

                    storages = filteredStorages;
                }
            }
        }

        assertTrue(storages.size() == 1);
    }
}
