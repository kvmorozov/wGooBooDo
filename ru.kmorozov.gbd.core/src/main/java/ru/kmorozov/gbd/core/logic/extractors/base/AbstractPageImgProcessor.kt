package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.io.InputStream
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
abstract class AbstractPageImgProcessor<T : AbstractPage> : AbstractHttpProcessor, IUniqueReusable<T> {
    protected lateinit var bookContext: BookContext
    override lateinit var uniqueObject: T
    protected lateinit var usedProxy: HttpHostExt
    override var reuseCallback: (IUniqueReusable<T>) -> Unit = {}
    protected lateinit var logger: Logger
    protected abstract val successMsg: String

    protected constructor(bookContext: BookContext, page: T, usedProxy: HttpHostExt) : super() {
        initProcessor(bookContext, page, usedProxy)
    }

    private fun initProcessor(bookContext: BookContext, page: T, usedProxy: HttpHostExt) {
        this.bookContext = bookContext
        this.uniqueObject = page
        this.usedProxy = usedProxy
        logger = ExecutionContext.INSTANCE.getLogger(javaClass, bookContext)
    }

    override fun initReusable(pattern: IUniqueReusable<T>): Boolean {
        if (pattern is AbstractPageImgProcessor<T>) {
            initProcessor(pattern.bookContext, pattern.uniqueObject, pattern.usedProxy)

            return true
        } else
            return false
    }

    @JvmOverloads
    protected fun processImage(imgUrl: String, proxy: HttpHostExt = HttpHostExt.NO_PROXY): Boolean {
        if (GBDOptions.secureMode && proxy.isLocal) return false

        var inputStream: InputStream = System.`in`
        lateinit var storedItem: IStoredItem

        if (uniqueObject.isLoadingStarted) return false

        try {
            getContent(imgUrl, proxy, false).use { resp ->
                inputStream = resp.content

                if (System.`in` == inputStream) {
                    logger.info(getErrorMsg(imgUrl, proxy))
                    return false
                }

                var read: Int
                val bytes = ByteArray(dataChunk)
                var firstChunk = true
                var reloadFlag: Boolean

                do {
                    read = inputStream.read(bytes)

                    if (read == -1)
                        break

                    if (firstChunk) {
                        if (uniqueObject.isLoadingStarted) return false

                        uniqueObject.isLoadingStarted = true
                        storedItem = bookContext.storage.getStoredItem(uniqueObject, resp.imageFormat)

                        reloadFlag = !storedItem.createdNew
                        if (reloadFlag)
                            if (GBDOptions.reloadImages)
                                storedItem.delete()
                            else {
                                uniqueObject.isDataProcessed = true
                                return false
                            }

                        if (proxy.isLocal)
                            logger.info("Started img ${if (reloadFlag) "RELOADING" else "processing"} for ${uniqueObject.pid} without Proxy")
                        else
                            logger.info("Started img ${if (reloadFlag) "RELOADING" else "processing"} for ${uniqueObject.pid} with $proxy Proxy")
                    }

                    firstChunk = false

                    storedItem.write(bytes, read)
                } while (true)

                bookContext.storage.storeItem(storedItem)

                if (storedItem.validate()) {
                    uniqueObject.isDataProcessed = true
                    uniqueObject.isScanned = true
                    uniqueObject.isFileExists = true

                    proxy.promoteProxy()

                    logger.info(successMsg)

                    return true
                } else {
                    proxy.reset()
                    storedItem.delete()
                    return false
                }
            }
        } catch (ste: SocketTimeoutException) {
            proxy.registerFailure()
        } catch (ste: SocketException) {
            proxy.registerFailure()
        } catch (ste: SSLException) {
            proxy.registerFailure()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (System.`in` != inputStream) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                if (uniqueObject.isLoadingStarted)
                    storedItem.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (uniqueObject.isLoadingStarted && !uniqueObject.isDataProcessed) {
                logger.info("Loading page ${uniqueObject.pid} failed!")
                try {
                    storedItem.delete()
                    proxy.reset()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            reuseCallback(this)
        }

        return false
    }

    protected abstract fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String

    override fun toString(): String {
        return "Page processor:$bookContext"
    }

    private val dataChunk = 4096
}