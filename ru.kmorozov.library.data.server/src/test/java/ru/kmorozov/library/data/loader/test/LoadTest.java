package ru.kmorozov.library.data.loader.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.loader.LoaderConfiguration;
import ru.kmorozov.library.data.loader.LocalDirectoryLoader;

import java.io.IOException;

/**
 * Created by km on 26.12.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LoaderConfiguration.class})
public class LoadTest {

    @Autowired
    private LocalDirectoryLoader loader;

    @Test
    public void categoryLoadTest() throws IOException {
        loader.clear();
        loader.load();
    }
}
