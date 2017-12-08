package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.handler.timeout.TimeoutException;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.HttpResponseStatus;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;

import java.net.ConnectException;

/**
 * Created by km on 23.12.2016.
 */
public class AsyncHandler extends AsyncCompletionHandlerBase {

    private final HttpHostExt proxy;

    public AsyncHandler(final HttpHostExt proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onThrowable(final Throwable t) {
        if (t instanceof ConnectException) proxy.registerFailure();
        else if (t instanceof TimeoutException) proxy.registerFailure();
        else
            super.onThrowable(t);
    }

    @Override
    public State onStatusReceived(final HttpResponseStatus status) throws Exception {
        final int statusCode = status.getStatusCode();

        return 200 == statusCode ? super.onStatusReceived(status) : State.ABORT;
    }

}
