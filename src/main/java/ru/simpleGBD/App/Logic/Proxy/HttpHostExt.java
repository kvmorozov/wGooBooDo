package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;

/**
 * Created by km on 29.11.2015.
 */
public class HttpHostExt {

    private HttpHost host;
    private boolean available = true;

    public HttpHostExt(HttpHost host) {
        this.host = host;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
}
