package ru.kmorozov.gbd.core.logic.connectors.apache

import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.connectors.Response.Companion.EMPTY_RESPONSE
import ru.kmorozov.gbd.logger.Logger

import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Created by km on 17.05.2016.
 */
class ApacheHttpConnector : HttpConnector() {

    @Throws(IOException::class)
    override fun getContent(rqUrl: String, proxy: HttpHostExt, withTimeout: Boolean): Response {
        if (GBDOptions.secureMode() && proxy.isLocal || !proxy.isAvailable) return EMPTY_RESPONSE

        val response = getContent(ApacheConnections.INSTANCE.getClient(proxy, withTimeout), HttpGet(rqUrl), proxy, 0)

        if (response.empty) logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()))

        return response
    }

    private fun getContent(client: HttpClient, req: HttpGet, proxy: HttpHostExt, attempt: Int): Response {
        var _attempt = attempt
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) return EMPTY_RESPONSE

        if (0 < attempt)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.requestUri))
                Thread.sleep((HttpConnector.SLEEP_TIME * attempt).toLong())
            } catch (ignored: InterruptedException) {
            }

        try {
            return ApacheResponse(client.execute(req) as CloseableHttpResponse)
        } catch (ste1: SocketTimeoutException) {
            proxy.registerFailure()
            return getContent(client, req, proxy, ++_attempt)
        } catch (ex: Exception) {
            proxy.registerFailure()
            return EMPTY_RESPONSE
        }

    }

    override fun close() {
        ApacheConnections.INSTANCE.closeAllConnections()
    }

    companion object {

        private val logger = Logger.getLogger(HttpConnector::class.java)
    }
}
