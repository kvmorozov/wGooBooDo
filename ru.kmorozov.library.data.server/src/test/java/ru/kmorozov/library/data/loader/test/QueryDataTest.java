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

import static org.hamcrest.CoreMatchers.*;

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoConfiguration.class)
public class QueryDataTest {

    @Autowired
    private StorageRepository storageRepository;

    @Test
    public void storageQueryTest() {
        List<Storage> topStorages = this.storageRepository.findAllByParent(null);
        Assert.assertThat(topStorages.size(), is(1));

        List<Storage> level1Storages = this.storageRepository.findAllByParent(topStorages.get(0));
        Assert.assertThat(level1Storages.size(), is(4));
    }
}
