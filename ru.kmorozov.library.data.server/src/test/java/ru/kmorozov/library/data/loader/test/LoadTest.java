package ru.kmorozov.library.data.loader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.LocalDirectoryLoader;
import ru.kmorozov.library.data.loader.OneDriveLoader;

import java.io.IOException;

/**
 * Created by km on 26.12.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LoaderConfiguration.class})
public class LoadTest {

    @Autowired
    private LocalDirectoryLoader fsLoader;

    @Autowired
    private OneDriveLoader oneLoader;

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
        oneLoader.processLinks();
    }

    @Test
    public void loadLinksTestOne() throws IOException {
        oneLoader.processLinks();
    }
}
