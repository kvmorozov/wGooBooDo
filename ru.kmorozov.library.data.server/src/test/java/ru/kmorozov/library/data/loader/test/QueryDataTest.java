package ru.kmorozov.library.data.loader.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.model.book.Storage;
import ru.kmorozov.library.data.repository.StorageRepository;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoConfiguration.class})
public class QueryDataTest {

    @Autowired
    private StorageRepository storageRepository;

    @Test
    public void storageQueryTest() {
        List<Storage> topStorages = storageRepository.findAllByParent(null);
        Assert.assertEquals(1, topStorages.size());

        List<Storage> level1Storages = storageRepository.findAllByParent(topStorages.get(0));
        Assert.assertEquals(4, level1Storages.size());
    }
}
