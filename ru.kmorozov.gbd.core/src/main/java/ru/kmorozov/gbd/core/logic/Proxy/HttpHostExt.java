package ru.kmorozov.gbd.core.logic.Proxy;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.base.Strings;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.HttpConnections;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 29.11.2015.
 */
public class HttpHostExt {

    public static final HttpHostExt NO_PROXY = new HttpHostExt();

    private static final Logger logger = Logger.getLogger(HttpHostExt.class);
    private static final GenericUrl checkProxyUrl = new GenericUrl("http://mxtoolbox.com/WhatIsMyIP/");
    private static final int REMOTE_FAILURES_THRESHOLD = 15;
    private static final int LOCAL_FAILURES_THRESHOLD = 50;
    private static final String NO_PROXY_STR = "NO_PROXY";
    private final AtomicBoolean available;
    private final AtomicInteger failureCount;
    private InetSocketAddress host;
    private Proxy proxy;
    private String cookie;
    private boolean isSecure = true;
    private volatile HttpHeaders headers;

    private volatile long lastUsedTimestamp;

    public HttpHostExt(InetSocketAddress host, String cookie) {
        this.host = host;
        this.cookie = cookie;

        if (GBDOptions.secureMode()) this.isSecure = this.checkSecurity();

        this.failureCount = new AtomicInteger(0);
        this.available = new AtomicBoolean(true);
    }

    HttpHostExt(InetSocketAddress host, int failureCount) {
        this.host = host;
        this.failureCount = new AtomicInteger(failureCount);

        this.available = new AtomicBoolean(HttpHostExt.REMOTE_FAILURES_THRESHOLD >= failureCount);
    }

    private HttpHostExt() {
        this.proxy = Proxy.NO_PROXY;
        this.failureCount = new AtomicInteger(0);
        this.available = new AtomicBoolean(true);
    }

    public static HttpHostExt getProxyFromString(String proxyStr) {
        String[] proxyVars = proxyStr.split(";");
        return new HttpHostExt(new InetSocketAddress(proxyVars[0], Integer.parseInt(proxyVars[1])), Integer.parseInt(proxyVars[2]));
    }

    private boolean checkSecurity() {
        HttpRequestFactory requestFactory = new NetHttpTransport.Builder().setProxy(getProxy()).build().createRequestFactory();

        try {
            HttpResponse resp = requestFactory.buildGetRequest(HttpHostExt.checkProxyUrl).execute();
            if (null != resp) {
                try (final InputStream is = resp.getContent()) {
                    if (null != is) {
                        String respStr = new String(is.readAllBytes(), Charset.defaultCharset());
                        return !respStr.contains(InetAddress.getLocalHost().getHostName());
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public boolean isAvailable() {
        return this.available.get();
    }

    public boolean isNotAvailable() {
        return !this.available.get();
    }

    public InetSocketAddress getHost() {
        return this.host;
    }

    @Override
    public int hashCode() {
        return null == this.host ? -1 : this.host.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return !(null == obj || !(obj instanceof HttpHostExt)) && this.host.equals(((HttpHostExt) obj).host);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", null == this.host ? HttpHostExt.NO_PROXY_STR : this.host.toString(), -1 * this.failureCount.get());
    }

    public void registerFailure() {
        if (!this.isAvailable()) return;

        this.failureCount.incrementAndGet();
        if (this.failureCount.get() > (this.isLocal() ? HttpHostExt.LOCAL_FAILURES_THRESHOLD : HttpHostExt.REMOTE_FAILURES_THRESHOLD)) {
            synchronized (this) {
                if (this.isAvailable()) {
                    HttpHostExt.logger.info(String.format("Proxy %s invalidated!", null == this.host ? HttpHostExt.NO_PROXY_STR : this.host.toString()));
                    this.available.set(false);
                    AbstractProxyListProvider.getInstance().invalidatedProxyListener();
                }
            }
        }
    }

    public void forceInvalidate(boolean reportFailure) {
        synchronized (this) {
            if (this.isAvailable()) {
                this.failureCount.addAndGet(5);
                this.available.set(false);
                if (reportFailure)
                    HttpHostExt.logger.info(String.format("Proxy %s force-invalidated!", null == this.host ? HttpHostExt.NO_PROXY_STR : this.host.toString()));
            }
        }
    }

    public void promoteProxy() {
        if (!this.isLocal()) this.failureCount.decrementAndGet();
    }

    public Proxy getProxy() {
        if (null == this.proxy) this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.host.getHostName(), this.host.getPort()));

        return this.proxy;
    }

    public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public boolean isSecure() {
        return this.isSecure;
    }

    public boolean isLocal() {
        return this == HttpHostExt.NO_PROXY;
    }

    public boolean isSameAsStr(String proxyStr) {
        return !Strings.isNullOrEmpty(proxyStr) && proxyStr.equals(this.getProxyStringShort());
    }

    public void update(HttpHostExt anotherHost) {
        this.failureCount.set(this.failureCount.get() + anotherHost.failureCount.get());
    }

    public String getProxyString() {
        return this.host.getAddress().getHostAddress() + ';' + this.host.getPort() + ';' + this.failureCount.get();
    }

    public String getProxyStringShort() {
        return this.host.getAddress().getHostAddress() + ':' + this.host.getPort();
    }

    public HttpHeaders getHeaders(final UrlType urlType) {
        if (null == this.headers || null == this.headers.getCookie()) {
            synchronized (this) {
                if (null == this.headers || null == this.headers.getCookie()) {
                    this.headers = HttpConnections.getHeaders(this);
                    if (null == this.headers.getCookie()) this.headers.setCookie(HttpConnections.getCookieString(this.host, urlType));
                    if (null == this.headers.getCookie()) {
                        HttpHostExt.logger.severe(String.format("Cannot get cookies for proxy %s", toString()));
                        this.forceInvalidate(false);
                    }
                }
            }
        }

        return this.headers;
    }

    public void updateTimestamp() {
        this.lastUsedTimestamp = System.currentTimeMillis();
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }
}
