package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 29.11.2015.
 */
public class HttpHostExt {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, HttpHostExt.class.getName());

    public static final int FAILURES_THRESHOLD = 5;

    private HttpHost host;
    private AtomicBoolean available = new AtomicBoolean(true);
    private AtomicInteger failureCount = new AtomicInteger(0);

    public HttpHostExt(HttpHost host) {
        this.host = host;
    }

    public boolean isAvailable() {
        return available.get();
    }

    public HttpHost getHost() {
        return host;
    }

    @Override
    public int hashCode() {
        return host.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return host.equals(((HttpHostExt) obj).getHost());
    }

    @Override
    public String toString() {
        return host == null ? "no" : host.toHostString();
    }

    public void registerFailure() {
        failureCount.incrementAndGet();
        if (failureCount.get() > FAILURES_THRESHOLD) {
            logger.info(String.format("Proxy %s invalidated!", host.toHostString()));
            available.set(false);
        }
    }
}
