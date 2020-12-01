package ru.kmorozov.gbd.core.logic.model.book.base

import ru.kmorozov.gbd.core.config.IStoredItem
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by km on 18.11.2016.
 */
abstract class AbstractPage : IPage {

    private val dataProcessed = AtomicBoolean(false)
    private val fileExists = AtomicBoolean(false)
    public val loadingStarted = AtomicBoolean(false)
    private val scanned = AtomicBoolean(false)

    override var isDataProcessed: Boolean
        get() = dataProcessed.get()
        set(value) = dataProcessed.set(value)

    override var isFileExists: Boolean
        get() = fileExists.get()
        set(value) = fileExists.set(value)

    override var isLoadingStarted: Boolean
        get() = loadingStarted.get()
        set(value) = loadingStarted.set(value)

    override var isScanned: Boolean
        get() = scanned.get()
        set(value) = scanned.set(value)

    val isProcessed: Boolean
        get() = isDataProcessed || isFileExists

    val isNotProcessed: Boolean
        get() = !isProcessed

    abstract val imgUrl: String

    val isGapPage: Boolean
        get() = false

    override lateinit var storedItem: IStoredItem
}
