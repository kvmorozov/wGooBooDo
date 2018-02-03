package ru.kmorozov.gbd.core.logic.Proxy;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.utils.HttpConnections;
import ru.kmorozov.gbd.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
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

    public HttpHostExt(final InetSocketAddress host, final String cookie) {
        this.host = host;
        this.cookie = cookie;

        if (GBDOptions.secureMode()) isSecure = checkSecurity();

        failureCount = new AtomicInteger(0);
        available = new AtomicBoolean(true);
    }

    HttpHostExt(final InetSocketAddress host, final int failureCount) {
        this.host = host;
        this.failureCount = new AtomicInteger(failureCount);

        available = new AtomicBoolean(REMOTE_FAILURES_THRESHOLD >= failureCount);
    }

    private HttpHostExt() {
        proxy = Proxy.NO_PROXY;
        failureCount = new AtomicInteger(0);
        available = new AtomicBoolean(true);
    }

    public static HttpHostExt getProxyFromString(final String proxyStr) {
        final String[] proxyVars = proxyStr.split(";");
        return new HttpHostExt(new InetSocketAddress(proxyVars[0], Integer.parseInt(proxyVars[1])), Integer.parseInt(proxyVars[2]));
    }

    private boolean checkSecurity() {
        final HttpRequestFactory requestFactory = new Builder().setProxy(this.getProxy()).build().createRequestFactory();

        try {
            final HttpResponse resp = requestFactory.buildGetRequest(checkProxyUrl).execute();
            if (null != resp) {
                try (InputStream is = resp.getContent()) {
                    if (null != is) {
                        final String respStr = IOUtils.toString(is, Charset.defaultCharset());
                        return !respStr.contains(InetAddress.getLocalHost().getHostName());
                    }
                }
            }
        } catch (final IOException e) {
            return false;
        }

        return false;
    }

    public boolean isAvailable() {
        return available.get();
    }

    public boolean isNotAvailable() {
        return !available.get();
    }

    public InetSocketAddress getHost() {
        return host;
    }

    @Override
    public int hashCode() {
        return null == host ? -1 : host.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return !(null == obj || !(obj instanceof HttpHostExt)) && host.equals(((HttpHostExt) obj).host);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", null == host ? NO_PROXY_STR : host.toString(), -1 * failureCount.get());
    }

    public void registerFailure() {
        if (!isAvailable()) return;

        failureCount.incrementAndGet();
        if (failureCount.get() > (isLocal() ? LOCAL_FAILURES_THRESHOLD : REMOTE_FAILURES_THRESHOLD)) {
            synchronized (this) {
                if (isAvailable()) {
                    logger.info(String.format("Proxy %s invalidated!", null == host ? NO_PROXY_STR : host.toString()));
                    available.set(false);
                    AbstractProxyListProvider.getInstance().invalidatedProxyListener();
                }
            }
        }
    }

    public void forceInvalidate(final boolean reportFailure) {
        synchronized (this) {
            if (isAvailable()) {
                failureCount.addAndGet(5);
                available.set(false);
                if (reportFailure) logger.info(String.format("Proxy %s force-invalidated!", null == host ? NO_PROXY_STR : host.toString()));
            }
        }
    }

    public void promoteProxy() {
        if (!isLocal()) failureCount.decrementAndGet();
    }

    public Proxy getProxy() {
        if (null == proxy) proxy = new Proxy(Type.HTTP, new InetSocketAddress(host.getHostName(), host.getPort()));

        return proxy;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(final String cookie) {
        this.cookie = cookie;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public boolean isLocal() {
        return this == NO_PROXY;
    }

    public boolean isSameAsStr(final String proxyStr) {
        return !Strings.isNullOrEmpty(proxyStr) && proxyStr.equals(getProxyStringShort());
    }

    public void update(final HttpHostExt anotherHost) {
        failureCount.set(failureCount.get() + anotherHost.failureCount.get());
    }

    public String getProxyString() {
        return host.getAddress().getHostAddress() + ';' + host.getPort() + ';' + failureCount.get();
    }

    public String getProxyStringShort() {
        return host.getAddress().getHostAddress() + ':' + host.getPort();
    }

    public HttpHeaders getHeaders() {
        if (null == headers || null == headers.getCookie()) {
            synchronized (this) {
                headers = HttpConnections.getHeaders(this);
                if (null == headers.getCookie()) headers.setCookie(HttpConnections.getCookieString(host));
                if (null == headers.getCookie()) {
                    logger.severe(String.format("Cannot get cookies for proxy %s", this.toString()));
                    forceInvalidate(false);
                }
            }
        }

        return headers;
    }
}
