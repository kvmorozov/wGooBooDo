package ru.kmorozov.gbd.client

import org.apache.commons.lang3.StringUtils
import ru.kmorozov.db.utils.Mapper
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.URL
import java.nio.charset.Charset
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
            if (null == resp || null == resp.content) {
                logger.info("Rest service is unavailable!")
                throw RestServiceUnavailableException()
            }

            try {
                resp.content.use { `is` -> return String(`is`.readAllBytes(), Charset.defaultCharset()) }
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

    protected fun <T> getCallResult(operation: String, resultClass: Class<T>, vararg parameters: RestParam): T? {
        val rqUrl = StringBuilder(restServiceBaseUrl + operation)

        if (null != parameters && 0 < parameters.size) {
            rqUrl.append('?')
            for (param in parameters)
                rqUrl.append(param.paramName).append('=').append(param.value).append('&')
        }

        val rawResult: String

        try {
            rawResult = getRawResult(rqUrl.toString())
        } catch (e: RestServiceUnavailableException) {
            logger.finest(String.format("Service %s call failed!", operation))
            return if (resultClass == Boolean::class.java) java.lang.Boolean.FALSE as T else null
        }

        if (StringUtils.isEmpty(rawResult)) {
            logger.finest(String.format("Service %s call failed!", operation))
            return null
        }

        return Mapper.getGson()!!.fromJson(rawResult, resultClass)
    }

    companion object {

        protected val logger = Logger.getLogger(AbstractRestClient::class.java)

        protected const val restServiceBaseUrl = "http://localhost:8080/"
    }
}
