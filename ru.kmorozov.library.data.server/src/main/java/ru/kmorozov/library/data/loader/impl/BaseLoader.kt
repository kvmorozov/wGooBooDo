package ru.kmorozov.library.data.loader.impl

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.loader.ILoader
import ru.kmorozov.library.data.loader.impl.LoaderExecutor.State
import ru.kmorozov.library.data.model.book.Storage
import java.io.IOException
import java.io.UncheckedIOException

/**
 * Created by sbt-morozov-kv on 14.03.2017.
 */
abstract class BaseLoader : ILoader, Runnable {
    protected var links: MutableList<Any> = ArrayList()
    @Volatile
    internal var state = State.STOPPED

    val isStopped: Boolean
        get() = State.STOPPED == state

    override fun run() {
        try {
            load()
        } catch (e: IOException) {
            this.state = State.STOPPED

            e.printStackTrace()
        } catch (e: UncheckedIOException) {
            this.state = State.STOPPED
            e.printStackTrace()
        }

    }

    abstract fun refresh(storage: Storage): Storage

    companion object {

        private val logger = Logger.getLogger(GBDOptions.debugEnabled, BaseLoader::class.java)
    }
}
