package ru.kmorozov.gbd.core.logic.extractors.google

import com.google.common.base.Strings
import com.google.gson.JsonParseException
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueReusable
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import javax.net.ssl.SSLException

open class SigProcessorInternal(
    private val bookContext: BookContext,
    private val proxy: HttpHostExt,
    override var uniqueObject: GooglePageInfo
) : AbstractHttpProcessor(), IUniqueReusable<GooglePageInfo> {

    var sigFound = false

    override var reuseCallback: (IUniqueReusable<GooglePageInfo>) -> Unit = {}

    override fun initReusable(pattern: IUniqueReusable<GooglePageInfo>): Boolean {
        this.uniqueObject = pattern.uniqueObject

        return true
    }

    override fun run() {
        if (!proxy.isAvailable) return

        if (uniqueObject.isDataProcessed || uniqueObject.isLoadingStarted)
            return

        var response: Response = Response.EMPTY_RESPONSE
        val baseUrl =
            GoogleConstants.HTTPS_TEMPLATE.replace(GoogleConstants.BOOK_ID_PLACEHOLDER, bookContext.bookInfo.bookId)
        val rqUrl = baseUrl + GoogleConstants.PAGES_REQUEST_TEMPLATE.replace(
            GoogleConstants.RQ_PG_PLACEHOLDER,
            uniqueObject.pid
        )

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

            listOf(*framePages.pages)
                .stream()
                .filter { page -> null != (page as GooglePageInfo).src }
                .forEach { framePage ->
                    val page = bookContext.bookInfo.pages.getPageByPid(framePage.pid) as GooglePageInfo

                    if (page.isDataProcessed) return@forEach

                    val frameSrc = (framePage as GooglePageInfo).src
                    if (!Strings.isNullOrEmpty(frameSrc))
                        if (GBDOptions.debugEnabled)
                            logger.info("Sig candidate found $frameSrc")

                    if (page.addSrc(frameSrc!!)) {
                        if (page.pid == uniqueObject.pid) {
                            proxy.promoteProxy()

                            // Если есть возможность - пытаемся грузить страницу сразу
                            if (!GBDOptions.serverMode)
                                bookContext.imgExecutor.execute(GooglePageImgProcessor(bookContext, page, proxy))

                            sigFound = true
                        }
                    } else
                        logger.finest(String.format(SIG_WRONG_FORMAT, page.src))
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
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            response.close()

            reuseCallback(this)
        }
    }

    companion object {
        protected val logger = ExecutionContext.getLogger(SigProcessorInternal::class.java)

        private const val SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s"
        private const val SIG_WRONG_FORMAT = "Wrong sig format: %s"
    }
}