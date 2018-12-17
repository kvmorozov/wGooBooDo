package ru.kmorozov.library.data.loader.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.Storage
import ru.kmorozov.library.data.server.condition.StorageEnabledCondition

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by sbt-morozov-kv on 04.04.2017.
 */

@Component
@Conditional(StorageEnabledCondition::class)
class LoaderExecutor {

    @Autowired
    @Lazy
    private val oneLoader: OneDriveLoader? = null

    val isStarted: Boolean
        get() = State.STARTED == oneLoader!!.state

    enum class State {
        STARTED, STOPPED, PAUSED
    }

    @Synchronized
    fun start() {
        if (State.STARTED == oneLoader!!.state)
            throw IllegalStateException("Loader is already started!")

        oneLoader.state = State.STARTED

        loaderExecutor.execute(oneLoader)
    }

    @Synchronized
    fun stop() {
        oneLoader!!.state = State.STOPPED
    }

    @Synchronized
    fun pause() {
        oneLoader!!.state = State.PAUSED
    }

    fun refresh(storage: Storage): Storage {
        return oneLoader!!.refresh(storage)
    }

    fun resolveLink(lnkBook: Book) {
        oneLoader!!.resolveLink(lnkBook)
    }

    fun downloadBook(book: Book) {
        oneLoader!!.downloadBook(book)
    }

    companion object {

        private val loaderExecutor = Executors.newSingleThreadExecutor()
    }
}
