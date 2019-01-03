package ru.kmorozov.gbd.core.logic.extractors.base

import com.google.api.client.http.HttpStatusCodes
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.core.logic.connectors.ResponseException
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLException

/**
 * Created by km on 05.12.2015.
 */
open class AbstractHttpProcessor {

    protected fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        try {
            var resp: Response = EMPTY_RESPONSE
            for (connector in connectors) {
                resp = connector.getContent(rqUrl, proxy, withTimeout)

                if (!resp.empty)
                    return resp
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

    companion object {

        public val connectors: List<HttpConnector>
            get() = ExecutionContext.INSTANCE.defaultMetadata.preferredConnectors()

        private val LOCK = Any()

        fun close() {
            connectors.forEach(HttpConnector::close)
        }
    }
}