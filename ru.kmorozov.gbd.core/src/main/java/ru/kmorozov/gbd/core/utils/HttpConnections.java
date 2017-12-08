package ru.kmorozov.gbd.core.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 22.11.2015.
 */
public final class HttpConnections {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.1.871 Yowser/2.5 Safari/537.36";
    private static final HttpHeaders headers = new HttpHeaders().setUserAgent(USER_AGENT);
    private static final HttpConnections INSTANCE = new HttpConnections();
    private static GenericUrl baseUrl;

    private final Map<HttpHostExt, HttpHeaders> headersMap = new ConcurrentHashMap<>();

    private HttpConnections() {
    }

    private static Proxy hostToProxy(final InetSocketAddress host) {
        return null == host ? null : new Proxy(Type.HTTP, host);
    }

    public static HttpResponse getResponse(final InetSocketAddress host) {
        return _getResponse(hostToProxy(host));
    }

    public static HttpHeaders getHeaders(final HttpHostExt proxy) {
        return INSTANCE._getHeaders(proxy);
    }

    public static String getCookieString(final InetSocketAddress proxy) {
        try {
            final HttpResponse resp = getResponse(proxy);

            if (null == resp) return "";

            return ((List<String>) resp.getHeaders().get("set-cookie")).get(0).split(";")[0];
        } catch (final Exception e) {
            return null;
        }
    }

    private static void _setDefaultCookies(final Map<String, String> cookiesMap) {
        final StringBuilder cookieBuilder = new StringBuilder();

        for (final Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey()).append('=').append(cookieEntry.getValue()).append("; ");
        }

        HttpHostExt.NO_PROXY.setCookie(cookieBuilder.toString());
    }

    private HttpHeaders _getHeaders(final HttpHostExt proxy) {
        return headersMap.computeIfAbsent(proxy, httpHostExt -> {
            final HttpHeaders _headers = new HttpHeaders();
            _headers.setUserAgent(USER_AGENT);
            _headers.setCookie(httpHostExt.getCookie());

            return _headers;
        });
    }

    private static HttpResponse _getResponse(final Proxy proxy) {
        if (null == baseUrl) {
            final Optional<BookContext> anyContext = ExecutionContext.INSTANCE.getContexts(false).stream().filter(bookContext -> bookContext.getBookInfo().getBookData() instanceof
                    GoogleBookData).findAny();
            if (!anyContext.isPresent()) return null;
            else {
                baseUrl = new GenericUrl(((GoogleBookData) anyContext.get().getBookInfo().getBookData()).getBaseUrl());
            }
        }

        try {
            return new Builder().setProxy(proxy).build().createRequestFactory().buildGetRequest(baseUrl).setHeaders(headers).setConnectTimeout(10000).execute();
        } catch (final IOException e) {
            return null;
        }
    }
}
