package ru.kmorozov.gbd.core.logic.extractors.base

import com.google.api.client.http.HttpStatusCodes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.core.logic.connectors.ResponseException
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.proxy.AbstractProxyListProvider
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.SocketException
import java.nio.charset.Charset
import java.util.*
import javax.net.ssl.SSLException

/**
 * Created by km on 05.12.2015.
 */
open class AbstractHttpProcessor {

    protected fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        try {
            if (GBDOptions.secureMode && proxy.isLocal) return EMPTY_RESPONSE

            var resp: Response = EMPTY_RESPONSE
            for (connector in connectors) {
                resp = connector.getContent(rqUrl, proxy, withTimeout)

                if (!resp.empty) {
                    if (GBDOptions.debugEnabled && !resp.imageFormat.contentEquals("json"))
                        logger.info("Headers: ${resp.headers}")

                    return resp
                }
            }

            if (resp.empty && !proxy.isLocal)
                return getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
            else
                return resp
        } catch (re: ResponseException) {
            when (re.statusCode) {
                HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE, 413 -> proxy.forceInvalidate(true)
                HttpStatusCodes.STATUS_CODE_NOT_FOUND, HttpStatusCodes.STATUS_CODE_BAD_GATEWAY -> proxy.forceInvalidate(true)
                else -> re.printStackTrace()
            }

            return if (proxy.isLocal) EMPTY_RESPONSE else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (se: SocketException) {
            proxy.registerFailure()
            return if (proxy.isLocal) EMPTY_RESPONSE else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (se: SSLException) {
            proxy.registerFailure()
            return if (proxy.isLocal) EMPTY_RESPONSE else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (ioe: IOException) {
            proxy.registerFailure()

            // Если что-то более специфическое
            if (ioe.javaClass != IOException::class.java) ioe.printStackTrace()

            return if (proxy.isLocal) EMPTY_RESPONSE else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return EMPTY_RESPONSE
        }

    }

    protected fun getDocumentWithProxy(url: String, proxy: HttpHostExt): Optional<Document> {
        val resp = getContent(url, proxy, true)

        if (resp.empty)
            return Optional.empty()
        else {
            try {
                resp.content.use { `is` ->
                    val respStr = String(`is`.readAllBytes(), Charset.defaultCharset())
                    return Optional.of(Jsoup.parse(respStr))
                }
            } catch (e: IOException) {
                return Optional.empty()
            }
        }
    }

    protected fun getSomeProxy(): HttpHostExt {
        return AbstractProxyListProvider.INSTANCE.getSomeProxy()
    }

    companion object {
        private val logger = Logger.getLogger(AbstractHttpProcessor::class.java)

        public val connectors: List<HttpConnector>
            get() = ExecutionContext.INSTANCE.defaultMetadata.preferredConnectors()

        fun close() {
            connectors.forEach(HttpConnector::close)
        }
    }
}