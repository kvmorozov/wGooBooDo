package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.gson.JsonParseException
import org.apache.commons.lang3.StringUtils
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
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage
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
internal class GooglePageSigProcessor : AbstractHttpProcessor, IUniqueRunnable<GooglePageSigProcessor> {
    private val bookContext: BookContext
    private val proxy: HttpHostExt

    constructor(bookContext: BookContext, proxy: HttpHostExt) : super() {
        this.bookContext = bookContext
        this.proxy = proxy
        sigPageExecutor = QueuedThreadPoolExecutor(bookContext.pagesStream.filter { p -> (p as AbstractPage).isNotProcessed }.count(), QueuedThreadPoolExecutor.THREAD_POOL_SIZE, { it.isProcessed },
                bookContext.toString() + '/'.toString() + proxy)
        uniqueObject = this
    }

    private val sigPageExecutor: QueuedThreadPoolExecutor<GooglePageInfo>

    override var uniqueObject: GooglePageSigProcessor

    override fun run() {
        if (GBDOptions.secureMode() && proxy.isLocal || !proxy.isAvailable) return

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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true

        if (null == o || javaClass != o.javaClass) return false

        val that = o as GooglePageSigProcessor?

        return EqualsBuilder().append(proxy, that!!.proxy).append(bookContext, that.bookContext).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37).append(proxy).append(bookContext).toHashCode()
    }

    private inner class SigProcessorInternal internal constructor(override var uniqueObject: GooglePageInfo) : IUniqueRunnable<GooglePageInfo> {

        override fun run() {
            if (!proxy.isAvailable) return

            if (uniqueObject.isDataProcessed || null != uniqueObject.sig || uniqueObject.isSigChecked || uniqueObject.isLoadingStarted)
                return

            var resp: Response = Response.EMPTY_RESPONCE
            val baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookContext.bookInfo.bookId)
            val rqUrl = baseUrl + PAGES_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, uniqueObject.pid)

            try {
                resp = getContent(rqUrl, proxy, true)
                if (resp.empty) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()))
                    return
                }

                var respStr: String? = null
                try {
                    resp.content.use { respStr = String(it.readAllBytes(), Charset.defaultCharset()) }
                } catch (se: SocketException) {

                } catch (se: SSLException) {
                }

                if (StringUtils.isEmpty(respStr)) {
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
                            if (null != _frameSrc) _page.src = _frameSrc

                            if (null != _page.sig) {
                                if (_page.pid == uniqueObject.pid) {
                                    _page.isSigChecked = true

                                    proxy.promoteProxy()

                                    // Если есть возможность - пытаемся грузить страницу сразу
                                    bookContext.imgExecutor.execute(GooglePageImgProcessor(bookContext, _page, proxy))
                                }
                            }

                            if (null != _page.src && null == _page.sig)
                                logger.finest(String.format(SIG_WRONG_FORMAT, _page.src))
                        }
            } catch (ce: SocketTimeoutException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info(String.format("Proxy %s failed!", proxy.toString()))
                }

            } catch (ce: SocketException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info(String.format("Proxy %s failed!", proxy.toString()))
                }
                if (ce !is SocketTimeoutException) ce.printStackTrace()
            } catch (ce: NoHttpResponseException) {
                if (!proxy.isLocal) {
                    proxy.registerFailure()
                    logger.info(String.format("Proxy %s failed!", proxy.toString()))
                }
                if (ce !is SocketTimeoutException) ce.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                resp.close()
            }
        }
    }

    companion object {

        protected val logger = ExecutionContext.getLogger(GooglePageSigProcessor::class.java)

        private const val SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s"
        private const val SIG_WRONG_FORMAT = "Wrong sig format: %s"
    }
}
