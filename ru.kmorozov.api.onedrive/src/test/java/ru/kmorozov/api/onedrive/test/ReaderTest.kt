package ru.kmorozov.api.onedrive.test

import ru.kmorozov.onedrive.client.OneDriveProvider
import ru.kmorozov.onedrive.client.OneDriveProvider.FACTORY
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import ru.kmorozov.onedrive.client.walker.OneDriveWalkers
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test

import java.io.File
import java.io.IOException

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
class ReaderTest {

    private lateinit var api: OneDriveProvider

    @Before
    @Throws(IOException::class)
    fun initApi() {
        val file = File(javaClass.classLoader.getResource("onedrive.key")!!.file)

        val authoriser = AuthorisationProvider.FACTORY.create(file.toPath(), "", "")
        api = FACTORY.readOnlyApi(authoriser)
    }

    @Test
    @Throws(IOException::class)
    fun rootTest() {
        val root = api.root

        MatcherAssert.assertThat(root, CoreMatchers.`is`(CoreMatchers.notNullValue()))

        val children = api.getChildren(root!!)
        MatcherAssert.assertThat(children, CoreMatchers.`is`(CoreMatchers.notNullValue()))
    }

    @Test
    @Throws(IOException::class)
    fun walkTest() {
        OneDriveWalkers.walk(api, 3).forEach { oneDriveItem -> println(oneDriveItem.name) }
    }
}
