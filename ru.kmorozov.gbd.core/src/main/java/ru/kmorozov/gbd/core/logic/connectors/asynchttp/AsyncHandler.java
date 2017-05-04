package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.TimeoutException;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.HttpResponseStatus;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;

import java.net.ConnectException;

/**
 * Created by km on 23.12.2016.
 */
public class AsyncHandler extends AsyncCompletionHandlerBase {

    private HttpHostExt proxy;

    public AsyncHandler(HttpHostExt proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof ConnectException) proxy.registerFailure();
        else if (t instanceof TimeoutException) proxy.registerFailure();
        else
            super.onThrowable(t);
    }

    @Override
    public State onStatusReceived(HttpResponseStatus status) throws Exception {
        int statusCode = status.getStatusCode();

        return statusCode == 200 ? super.onStatusReceived(status) : State.ABORT;
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        return super.onHeadersReceived(headers);
    }
}
