package ru.kmorozov.library.data.loader.test

import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.kmorozov.library.data.loader.LoaderConfiguration
import ru.kmorozov.library.data.loader.impl.LocalDirectoryLoader
import ru.kmorozov.library.data.loader.impl.OneDriveLoader
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.repository.StorageRepository

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.regex.Pattern

import org.hamcrest.CoreMatchers.`is`

/**
 * Created by km on 26.12.2016.
 */

@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(LoaderConfiguration::class))
class LoadTest {

    @Autowired
    private val fsLoader: LocalDirectoryLoader? = null

    @Autowired
    private val oneLoader: OneDriveLoader? = null

    @Autowired
    protected var storageRepository: StorageRepository? = null

    @Test
    @Throws(IOException::class)
    fun categoryLoadTestFs() {
        //        fsLoader.clear();
        fsLoader!!.load()
        fsLoader.processLinks()
    }

    @Test
    @Throws(IOException::class)
    fun categoryLoadTestOne() {
        oneLoader!!.clear()
        oneLoader.load()
    }

    @Test
    @Throws(IOException::class)
    fun loadLinksTestOne() {
        oneLoader!!.processLinks()
    }

    @Test
    fun loadLinksTestOneMiltiDir() {
        val names = MULTIPLE_LNK_DIR.split(delimiter.toRegex()).dropLastWhile { it.isEmpty }.toTypedArray()
        var storages = storageRepository!!.findAllByName(names[names.size - 1])
        var parentName: String?

        var index = names.size - 1
        while (0 < index) {
            if (1 == storages.size)
                break
            else {
                parentName = names[index - 1]
                if (null != parentName) {
                    val filteredStorages = ArrayList<Storage>()
                    for (storage in storages)
                        if (storage.parent!!.name == parentName)
                            filteredStorages.add(storage)

                    storages = filteredStorages
                }
            }
            index--
        }

        MatcherAssert.assertThat(1, `is`(storages.size))
    }

    companion object {

        private const val MULTIPLE_LNK_DIR = "J:\\OneDrive\\_Книги\\Религиозные вопросы\\Христианство"
        private val delimiter = Pattern.quote(File.separator)
    }
}
