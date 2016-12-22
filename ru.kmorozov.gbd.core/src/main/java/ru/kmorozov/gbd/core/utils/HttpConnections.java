package ru.kmorozov.gbd.core.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.google.GoogleBookData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh_CN) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0 " + "baidubrowser/1.x Safari/534.7";
    private static final HttpHeaders headers = new HttpHeaders().setUserAgent(USER_AGENT);
    private static final HttpConnections INSTANCE = new HttpConnections();
    private static GenericUrl baseUrl = null;

    private final Map<HttpHostExt, HttpHeaders> headersMap = new ConcurrentHashMap<>();

    private HttpConnections() {
    }

    private static Proxy hostToProxy(InetSocketAddress host) {
        return host == null ? null : new Proxy(Proxy.Type.HTTP, host);
    }

    public static HttpResponse getResponse(InetSocketAddress host) {
        return INSTANCE._getResponse(hostToProxy(host));
    }

    public static HttpHeaders getHeaders(HttpHostExt proxy) {
        return INSTANCE._getHeaders(proxy);
    }

    public static String getCookieString(InetSocketAddress proxy) {
        try {
            HttpResponse resp = HttpConnections.getResponse(proxy);

            if (resp == null) return "";

            return ((ArrayList) resp.getHeaders().get("set-cookie")).get(0).toString().split(";")[0];
        } catch (Exception e) {
            return null;
        }
    }

    private void _setDefaultCookies(Map<String, String> cookiesMap) {
        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey()).append("=").append(cookieEntry.getValue()).append("; ");
        }

        HttpHostExt.NO_PROXY.setCookie(cookieBuilder.toString());
    }

    private HttpHeaders _getHeaders(final HttpHostExt proxy) {
        return headersMap.computeIfAbsent(proxy, httpHostExt -> {
            HttpHeaders _headers = new HttpHeaders();
            _headers.setUserAgent(USER_AGENT);
            _headers.setCookie(httpHostExt.getCookie());

            return _headers;
        });
    }

    private HttpResponse _getResponse(Proxy proxy) {
        if (baseUrl == null) {
            Optional<BookContext> anyContext = ExecutionContext.INSTANCE.getContexts(false).stream().filter(bookContext -> bookContext.getBookInfo().getBookData() instanceof
                    GoogleBookData).findAny();
            if (!anyContext.isPresent()) return null;
            else {
                baseUrl = new GenericUrl(((GoogleBookData) anyContext.get().getBookInfo().getBookData()).getBaseUrl());
            }
        }

        try {
            return new NetHttpTransport.Builder().setProxy(proxy).build().createRequestFactory().buildGetRequest(baseUrl).setHeaders(headers).setConnectTimeout(10000).execute();
        } catch (IOException e) {
            return null;
        }
    }
}
