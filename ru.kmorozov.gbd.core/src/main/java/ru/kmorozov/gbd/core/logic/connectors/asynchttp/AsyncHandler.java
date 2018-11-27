package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.handler.timeout.TimeoutException;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseStatus;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;

import java.net.ConnectException;

/**
 * Created by km on 23.12.2016.
 */
public class AsyncHandler extends AsyncCompletionHandlerBase {

    private final HttpHostExt proxy;

    public AsyncHandler(HttpHostExt proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof ConnectException) this.proxy.registerFailure();
        else if (t instanceof TimeoutException) this.proxy.registerFailure();
        else
            super.onThrowable(t);
    }

    @Override
    public org.asynchttpclient.AsyncHandler.State onStatusReceived(HttpResponseStatus status) throws Exception {
        int statusCode = status.getStatusCode();

        return 200 == statusCode ? super.onStatusReceived(status) : org.asynchttpclient.AsyncHandler.State.ABORT;
    }

}
