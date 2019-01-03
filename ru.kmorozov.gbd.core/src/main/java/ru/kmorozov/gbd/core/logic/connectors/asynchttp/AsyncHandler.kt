package ru.kmorozov.gbd.core.logic.connectors.asynchttp

import io.netty.handler.timeout.TimeoutException
import org.asynchttpclient.AsyncCompletionHandlerBase
import org.asynchttpclient.HttpResponseStatus
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt

import java.net.ConnectException

/**
 * Created by km on 23.12.2016.
 */
class AsyncHandler(private val proxy: HttpHostExt) : AsyncCompletionHandlerBase() {

    override fun onThrowable(t: Throwable) {
        if (t is ConnectException)
            proxy.registerFailure()
        else if (t is TimeoutException)
            proxy.registerFailure()
        else
            super.onThrowable(t)
    }

    @Throws(Exception::class)
    override fun onStatusReceived(status: HttpResponseStatus): org.asynchttpclient.AsyncHandler.State {
        val statusCode = status.statusCode

        return if (ok.contains(statusCode)) super.onStatusReceived(status) else org.asynchttpclient.AsyncHandler.State.ABORT
    }

    companion object {
        val ok = setOf<Int>(200, 302)
    }

}
