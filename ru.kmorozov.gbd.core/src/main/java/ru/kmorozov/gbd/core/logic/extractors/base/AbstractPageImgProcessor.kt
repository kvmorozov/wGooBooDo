package ru.kmorozov.gbd.core.logic.extractors.base

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.gbd.utils.Images
import java.io.IOException
import java.io.InputStream
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
abstract class AbstractPageImgProcessor<T : AbstractPage> : AbstractHttpProcessor, IUniqueRunnable<T> {
    protected val bookContext: BookContext
    override var uniqueObject: T
    protected val usedProxy: HttpHostExt

    protected constructor(bookContext: BookContext, uniqueObject: T, usedProxy: HttpHostExt) : super() {
        this.bookContext = bookContext
        this.uniqueObject = uniqueObject
        this.usedProxy = usedProxy
        logger = ExecutionContext.INSTANCE.getLogger(javaClass, bookContext)
    }

    protected val logger: Logger

    protected abstract val successMsg: String

    @JvmOverloads
    protected fun processImage(imgUrl: String, proxy: HttpHostExt = HttpHostExt.NO_PROXY): Boolean {
        if (GBDOptions.secureMode && proxy.isLocal) return false

        var inputStream: InputStream? = null
        lateinit var storedItem: IStoredItem

        if (uniqueObject.isLoadingStarted) return false

        try {
            getContent(imgUrl, proxy, false).use { resp ->
                inputStream = resp.content

                if (null == inputStream) {
                    logger.info(getErrorMsg(imgUrl, proxy))
                    return false
                }

                var read: Int
                val bytes = ByteArray(dataChunk)
                var firstChunk = true
                var reloadFlag: Boolean

                do {
                    read = inputStream!!.read(bytes)

                    if (read == -1)
                        break

                    if (firstChunk) {
                        val imgFormat = Images.getImageFormat(resp)

                        if (uniqueObject.isLoadingStarted) return false

                        uniqueObject.isLoadingStarted = true
                        storedItem = bookContext.storage.getStoredItem(uniqueObject, imgFormat)

                        reloadFlag = !storedItem.createdNew
                        if (reloadFlag)
                            if (GBDOptions.reloadImages)
                                storedItem.delete()
                            else {
                                uniqueObject.isDataProcessed = true
                                return false
                            }

                        if (proxy.isLocal)
                            logger.info(String.format("Started img %s for %s without Proxy", if (reloadFlag) "RELOADING" else "processing", uniqueObject.pid))
                        else
                            logger.info(String.format("Started img %s for %s with %s Proxy", if (reloadFlag) "RELOADING" else "processing", uniqueObject.pid, proxy.toString()))
                    }

                    firstChunk = false

                    storedItem.write(bytes, read)
                } while (true)

                if (validateOutput(storedItem, imgWidth)) {
                    uniqueObject.isDataProcessed = true

                    proxy.promoteProxy()

                    logger.info(successMsg)
                    uniqueObject.isDataProcessed = true
                    uniqueObject.isFileExists = true

                    return true
                } else {
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
            if (null != inputStream) {
                try {
                    inputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                storedItem.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (!uniqueObject.isDataProcessed) {
                logger.info(String.format("Loading page %s failed!", uniqueObject.pid))
                try {
                    storedItem.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        return false
    }

    protected abstract fun getErrorMsg(imgUrl: String, proxy: HttpHostExt): String

    override fun toString(): String {
        return "Page processor:$bookContext"
    }

    protected abstract fun validateOutput(storedItem: IStoredItem?, width: Int): Boolean

    private val dataChunk = 4096

    protected val imgWidth: Int
        get() = if (0 == GBDOptions.imageWidth) DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth
}