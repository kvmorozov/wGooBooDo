package ru.kmorozov.library.data.loader.test

import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.kmorozov.library.data.config.MongoConfiguration
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.repository.StorageRepository

import org.hamcrest.CoreMatchers.`is`

/**
 * Created by sbt-morozov-kv on 17.01.2017.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(MongoConfiguration::class))
class QueryDataTest {

    @Autowired
    private val storageRepository: StorageRepository? = null

    @Test
    fun storageQueryTest() {
        val topStorages = storageRepository!!.findAllByParent(null!!)
        MatcherAssert.assertThat(topStorages.size, `is`(1))

        val level1Storages = storageRepository.findAllByParent(topStorages[0])
        MatcherAssert.assertThat(level1Storages.size, `is`(4))
    }
}
