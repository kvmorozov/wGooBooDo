package ru.simpleGBD.App.Logic.Proxy;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 29.11.2015.
 */
public class HttpHostExt {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, HttpHostExt.class.getName());

    private static final GenericUrl checkProxyUrl = new GenericUrl("http://mxtoolbox.com/WhatIsMyIP/");

    private static final int REMOTE_FAILURES_THRESHOLD = 5;
    private static final int LOCAL_FAILURES_THRESHOLD = 50;
    public static final HttpHostExt NO_PROXY = new HttpHostExt();
    public static final String NO_PROXY_STR = "NO_PROXY";

    private HttpHost host;
    private Proxy proxy;
    private String cookie;
    private AtomicBoolean available = new AtomicBoolean(true);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private boolean isSecure = true;

    public HttpHostExt(HttpHost host, String cookie) {
        this.host = host;
        this.cookie = cookie;

        if (GBDOptions.secureMode())
            isSecure = checkSecurity();
    }

    private HttpHostExt() {
        proxy = Proxy.NO_PROXY;
    }

    private boolean checkSecurity() {
        HttpRequestFactory requestFactory = new NetHttpTransport.Builder().setProxy(this.getProxy()).build().createRequestFactory();

        try {
            HttpResponse resp = requestFactory.buildGetRequest(checkProxyUrl).execute();
            if (resp != null && resp.getContent() != null) {
                String respStr = IOUtils.toString(resp.getContent());
                return !respStr.contains(InetAddress.getLocalHost().getHostName());
            }
        } catch (IOException e) {
            return false;
        }

        return false;
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
        return host == null ? NO_PROXY_STR : String.format("%s (%d)", host.toHostString(), -1 * failureCount.get());
    }

    public void registerFailure() {
        if (isLocal() || !isAvailable())
            return;

        failureCount.incrementAndGet();
        if (failureCount.get() > (isLocal() ? LOCAL_FAILURES_THRESHOLD : REMOTE_FAILURES_THRESHOLD)) {
            logger.info(String.format("Proxy %s invalidated!", host.toHostString()));
            available.set(false);
            AbstractProxyListProvider.getInstance().invalidatedProxyListener();
        }
    }

    public void promoteProxy() {
        if (!isLocal())
            failureCount.decrementAndGet();
    }

    public Proxy getProxy() {
        if (proxy == null)
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host.getHostName(), host.getPort()));

        return proxy;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public boolean isLocal() {return this == HttpHostExt.NO_PROXY;}
}
