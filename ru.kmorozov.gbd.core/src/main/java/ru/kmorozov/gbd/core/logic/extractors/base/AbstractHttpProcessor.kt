package ru.kmorozov.gbd.core.logic.extractors.base

import com.google.api.client.http.HttpStatusCodes
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.ResponseException
import ru.kmorozov.gbd.core.logic.library.LibraryFactory

import javax.net.ssl.SSLException
import java.io.IOException
import java.net.SocketException
import java.util.ArrayList

/**
 * Created by km on 05.12.2015.
 */
open class AbstractHttpProcessor {

    protected fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response? {
        try {
            var resp: Response? = null
            for (connector in getConnectors()!!) {
                resp = connector.getContent(rqUrl, proxy, withTimeout)
                resp = if (proxy.isLocal) resp else resp ?: getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
                if (null != resp)
                    return resp
            }

            return resp
        } catch (re: ResponseException) {
            when (re.statusCode) {
                HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE, 413 -> proxy.forceInvalidate(true)
                HttpStatusCodes.STATUS_CODE_NOT_FOUND, HttpStatusCodes.STATUS_CODE_BAD_GATEWAY -> proxy.forceInvalidate(true)
                else -> re.printStackTrace()
            }

            return if (proxy.isLocal) null else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (se: SocketException) {
            proxy.registerFailure()
            return if (proxy.isLocal) null else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (se: SSLException) {
            proxy.registerFailure()
            return if (proxy.isLocal) null else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (ioe: IOException) {
            proxy.registerFailure()

            // Если что-то более специфическое
            if (ioe.javaClass != IOException::class.java) ioe.printStackTrace()

            return if (proxy.isLocal) null else getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }

    }

    companion object {

        private var connectors: List<HttpConnector>? = null
        private val LOCK = Any()

        private fun getConnectors(): List<HttpConnector>? {
            if (null == connectors || connectors!!.isEmpty())
                synchronized(LOCK) {
                    if (null == connectors || connectors!!.isEmpty())
                        connectors = LibraryFactory.preferredConnectors()
                }

            return connectors
        }

        fun close() {
            for (connector in getConnectors()!!) {
                try {
                    connector.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }
}