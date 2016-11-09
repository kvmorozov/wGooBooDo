package ru.kmorozov.gbd.core.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh_CN) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0 baidubrowser/1.x Safari/534.7";
    private static final HttpHeaders headers = new HttpHeaders().setUserAgent(USER_AGENT);
    private static GenericUrl baseUrl;

    private static final HttpConnections INSTANCE = new HttpConnections();

    private static Map<String, String> cookiesMap;

    private final Map<HttpHostExt, HttpHeaders> headersMap = new ConcurrentHashMap<>();

    private HttpConnections() {
    }

    private void _setDefaultCookies(Map<String, String> cookiesMap) {
        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey()).append("=").append(cookieEntry.getValue()).append("; ");
        }

        HttpHostExt.NO_PROXY.setCookie(cookieBuilder.toString());
    }

    private HttpHeaders _getHeaders(HttpHostExt proxy) {
        HttpHeaders headers = headersMap.get(proxy);

        if (headers == null) {
            headers = new HttpHeaders();
            headers.setUserAgent(USER_AGENT);
            headers.setCookie(proxy.getCookie());

            headersMap.put(proxy, headers);
        }

        return headers;
    }

    private HttpResponse _getResponse(Proxy proxy) {
        if (baseUrl == null) baseUrl = new GenericUrl(ExecutionContext.INSTANCE.getBaseUrl());

        try {
            return new NetHttpTransport.Builder().setProxy(proxy).build().createRequestFactory().buildGetRequest(baseUrl).setHeaders(headers).setConnectTimeout(10000).execute();
        } catch (IOException e) {
            return null;
        }
    }

    private static Proxy hostToProxy(InetSocketAddress host) {
        return new Proxy(Proxy.Type.HTTP, host);
    }

    public static HttpResponse getResponse(InetSocketAddress host) {
        return INSTANCE._getResponse(hostToProxy(host));
    }

    public static HttpHeaders getHeaders(HttpHostExt proxy) {
        return INSTANCE._getHeaders(proxy);
    }

    public static void setDefaultCookies(Map<String, String> _cookiesMap) {
        cookiesMap = _cookiesMap;

        INSTANCE._setDefaultCookies(cookiesMap);
    }

    public static Map<String, String> getCookiesMap() {
        return cookiesMap;
    }
}