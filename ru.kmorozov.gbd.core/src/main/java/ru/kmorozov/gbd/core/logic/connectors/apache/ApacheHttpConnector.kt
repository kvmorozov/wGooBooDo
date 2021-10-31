package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.core.logic.proxy.HttpHostExt
import ru.kmorozov.gbd.logger.Logger
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Created by km on 17.05.2016.
 */
class ApacheHttpConnector(private var factory: IApacheConnectionFactory) : HttpConnector() {

    @Throws(IOException::class)
    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        if (GBDOptions.secureMode && proxy.isLocal || !proxy.isAvailable) return EMPTY_RESPONSE

        val response = getContent(factory.getClient(proxy, withTimeout), HttpGet(rqUrl), proxy, 0)

        if (response.empty) logger.finest("No response at url $rqUrl with proxy $proxy")

        return response
    }

    private fun getContent(client: HttpClient, req: HttpGet, proxy: HttpHostExt, attempt: Int): Response {
        if (MAX_RETRY_COUNT <= attempt) return EMPTY_RESPONSE

        if (0 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.uri))
                Thread.sleep((SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return ApacheResponse(client.execute(req) as CloseableHttpResponse)
        } catch (ste1: SocketTimeoutException) {
            if (GBDOptions.debugEnabled)
                logger.info(ste1.stackTraceToString())

            proxy.registerFailure()
            return getContent(client, req, proxy, attempt + 1)
        } catch (ex: Exception) {
            if (GBDOptions.debugEnabled)
                logger.info(ex.stackTraceToString())

            proxy.registerFailure()
            return EMPTY_RESPONSE
        }

    }

    override fun close() {
        factory.closeAllConnections()
    }

    companion object {

        private val logger = Logger.getLogger(GBDOptions.debugEnabled, ApacheHttpConnector::class.java)
    }
}
