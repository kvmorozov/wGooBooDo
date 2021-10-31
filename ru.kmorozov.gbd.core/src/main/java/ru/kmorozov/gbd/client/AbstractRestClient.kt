package ru.kmorozov.gbd.client

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.URL
import javax.net.ssl.SSLException

/**
 * Created by km on 20.12.2016.
 */
abstract class AbstractRestClient : AbstractHttpProcessor() {

    protected class RestParam internal constructor(internal var paramName: String, internal var value: Any)

    fun serviceAvailable(): Boolean {
        try {
            Socket().use { socket ->
                val serviceURL = URL(restServiceBaseUrl)
                socket.connect(InetSocketAddress(serviceURL.host, serviceURL.port))
                return true
            }
        } catch (e: IOException) {
            return false
        }

    }

    @Throws(RestServiceUnavailableException::class)
    private fun getRawResult(rqUrl: String): String {
        try {
            val resp = getContent(rqUrl, HttpHostExt.NO_PROXY, true)
            if (resp.empty) {
                logger.info("Rest service is unavailable!")
                throw RestServiceUnavailableException()
            }

            try {
                resp.content.use { return String(it.readAllBytes()) }
            } catch (se: SocketException) {
                logger.info("Rest service is unavailable! " + se.message)
                throw RestServiceUnavailableException()
            } catch (se: SSLException) {
                logger.info("Rest service is unavailable! " + se.message)
                throw RestServiceUnavailableException()
            }

        } catch (ioe: IOException) {
            logger.info("Rest service is unavailable! " + ioe.message)
            throw RestServiceUnavailableException()
        }

    }

    companion object {

        protected val logger = Logger.getLogger(GBDOptions.debugEnabled, AbstractRestClient::class.java)

        protected const val restServiceBaseUrl = "http://localhost:8080/"
    }
}
