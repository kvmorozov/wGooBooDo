package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.common.base.Strings
import com.google.gson.JsonParseException
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.hc.core5.http.NoHttpResponseException
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.PAGES_REQUEST_TEMPLATE
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.RQ_PG_PLACEHOLDER
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueReusable
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/**
 * Created by km on 21.11.2015.
 */
internal class GooglePageSigProcessor : AbstractHttpProcessor, IUniqueReusable<GooglePageSigProcessor> {
    private lateinit var bookContext: BookContext
    private lateinit var proxy: HttpHostExt
    override lateinit var reuseCallback: (IUniqueReusable<GooglePageSigProcessor>) -> Unit
    private val sigPageExecutor: QueuedThreadPoolExecutor<GooglePageInfo>
    override lateinit var uniqueObject: GooglePageSigProcessor

    constructor(bookContext: BookContext, proxy: HttpHostExt) : super() {
        sigPageExecutor = QueuedThreadPoolExecutor(bookContext.pagesStream.filter { p -> (p as AbstractPage).isNotProcessed }.count().toInt(),
                QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { it.isProcessed },
                bookContext.toString() + '/'.toString() + proxy)
        initProcessor(bookContext, proxy)
    }

    private fun initProcessor(bookContext: BookContext, proxy: HttpHostExt) {
        this.bookContext = bookContext
        this.proxy = proxy
        uniqueObject = this
    }

    override fun initReusable(pattern: IUniqueReusable<GooglePageSigProcessor>): Boolean {
        if (pattern is GooglePageSigProcessor) {
            initProcessor(pattern.bookContext, pattern.proxy)

            return true
        } else
            return false
    }

    override fun run() {
        if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable) return

        if (!proxy.isLocal && !(proxy.isAvailable && 0 < proxy.host.port)) return

        val psSigs = bookContext.progress.getSubProgress(bookContext.bookInfo.pages.pages.size)

        bookContext.pagesStream.filter { p -> (p as AbstractPage).isNotProcessed }.forEach { page ->
            psSigs.inc()
            sigPageExecutor.execute(SigProcessorInternal(page as GooglePageInfo))
        }
        sigPageExecutor.terminate(3L, TimeUnit.MINUTES)

        psSigs.finish()
    }

    override fun toString(): String {
        return "Sig processor:$bookContext"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (null == other || javaClass != other.javaClass) return false

        val that = other as GooglePageSigProcessor?

        return EqualsBuilder().append(proxy, that!!.proxy).append(bookContext, that.bookContext).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37).append(proxy).append(bookContext).toHashCode()
    }

    private inner class SigProcessorInternal internal constructor(override var uniqueObject: GooglePageInfo) : IUniqueReusable<GooglePageInfo> {

        override lateinit var reuseCallback: (IUniqueReusable<GooglePageInfo>) -> Unit

        override fun initReusable(pattern: IUniqueReusable<GooglePageInfo>): Boolean {
            this.uniqueObject = pattern.uniqueObject

            return true
        }

        override fun run() {
            if (!proxy.isAvailable) return

            if (uniqueObject.isDataProcessed || uniqueObject.isLoadingStarted)
                return

            var response: Response = Response.EMPTY_RESPONSE
            val baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookContext.bookInfo.bookId)
            val rqUrl = baseUrl + PAGES_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, uniqueObject.pid)

            try {
                response = getContent(rqUrl, proxy, true)
                if (response.empty) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()))
                    return
                }

                var respStr: String? = null
                try {
                    response.content.use { respStr = String(it.readAllBytes(), Charset.defaultCharset()) }
                } catch (se: SocketException) {

                } catch (se: SSLException) {
                }

                if (Strings.isNullOrEmpty(respStr)) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()))
                    return
                }

                var framePages: GooglePagesInfo? = null
                try {
                    framePages = Mapper.gson.fromJson(respStr, GooglePagesInfo::class.java)
                } catch (jpe: JsonParseException) {
                    logger.severe("Invalid JSON string: " + respStr!!)
                }

                if (null == framePages) return

                Arrays.asList(*framePages.pages)
                        .stream()
                        .filter { page -> null != (page as GooglePageInfo).src }
                        .forEach { framePage ->
                            val _page = bookContext.bookInfo.pages.getPageByPid(framePage.pid) as GooglePageInfo

                            if (_page.isDataProcessed) return@forEach;

                            val _frameSrc = (framePage as GooglePageInfo).src
                            if (!Strings.isNullOrEmpty(_frameSrc))
                                if (GBDOptions.debugEnabled)
                                    logger.info("Sig candidate found ${_frameSrc}")

                                if (_page.addSrc(_frameSrc!!)) {
                                    if (_page.pid == uniqueObject.pid) {
                                        proxy.promoteProxy()

                                        // Если есть возможность - пытаемся грузить страницу сразу
                                        bookContext.imgExecutor.execute(GooglePageImgProcessor(bookContext, _page, proxy))
                                    }
                                } else
                                    logger.finest(String.format(SIG_WRONG_FORMAT, _page.src))
                        }
            } catch (ce: SocketTimeoutException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info("Proxy $proxy failed!")
                }
            } catch (ce: SocketException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info("Proxy $proxy failed!")
                }
                if (ce !is SocketTimeoutException) ce.printStackTrace()
            } catch (ce: NoHttpResponseException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info("Proxy $proxy failed!")
                }
                if (ce !is SocketTimeoutException) ce.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                response.close()

                reuseCallback(this)
            }
        }
    }

    companion object {
        protected val logger = ExecutionContext.getLogger(GooglePageSigProcessor::class.java)

        private const val SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s"
        private const val SIG_WRONG_FORMAT = "Wrong sig format: %s"
    }
}
