package ru.kmorozov.gbd.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.UrlType;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by km on 22.11.2015.
 */
public final class HttpConnections {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.1.871 Yowser/2.5 Safari/537.36";
    private static final HttpHeaders headers = new HttpHeaders().setUserAgent(HttpConnections.USER_AGENT);
    private static final HttpConnections INSTANCE = new HttpConnections();
    private static final Map<UrlType, GenericUrl> baseUrls = new HashMap<>(1);

    private final Map<HttpHostExt, HttpHeaders> headersMap = new ConcurrentHashMap<>();

    private HttpConnections() {
    }

    private static Proxy hostToProxy(InetSocketAddress host) {
        return null == host ? null : new Proxy(Proxy.Type.HTTP, host);
    }

    public static HttpResponse getResponse(InetSocketAddress host, final UrlType urlType) {
        return HttpConnections._getResponse(HttpConnections.hostToProxy(host), urlType);
    }

    public static HttpHeaders getHeaders(HttpHostExt proxy) {
        return HttpConnections.INSTANCE._getHeaders(proxy);
    }

    public static String getCookieString(InetSocketAddress proxy, final UrlType urlType) {
        try {
            HttpResponse resp = HttpConnections.getResponse(proxy, urlType);

            if (null == resp) return "";

            return ((Collection<String>) resp.getHeaders().get("set-cookie")).stream()
                    .map(s -> s.split(";")[0]).collect(Collectors.joining(";"));
        } catch (Exception e) {
            return null;
        }
    }

    private static void _setDefaultCookies(Map<String, String> cookiesMap) {
        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey()).append('=').append(cookieEntry.getValue()).append("; ");
        }

        HttpHostExt.NO_PROXY.setCookie(cookieBuilder.toString());
    }

    private HttpHeaders _getHeaders(HttpHostExt proxy) {
        return this.headersMap.computeIfAbsent(proxy, httpHostExt -> {
            HttpHeaders _headers = new HttpHeaders();
            _headers.setUserAgent(HttpConnections.USER_AGENT);
            _headers.setCookie(httpHostExt.getCookie());

            return _headers;
        });
    }

    private static GenericUrl getBaseUrl(final UrlType urlType) {
        GenericUrl url = HttpConnections.baseUrls.get(urlType);
        if (url == null) {
            switch (urlType) {
                case GOOGLE_BOOKS:
                    Optional<BookContext> anyContext = ExecutionContext.INSTANCE.getContexts(false).stream().filter(bookContext -> bookContext.getBookInfo().getBookData() instanceof
                            GoogleBookData).findAny();
                    if (!anyContext.isPresent()) return new GenericUrl("http://www.ya.ru");
                    else {
                        url = new GenericUrl(((GoogleBookData) anyContext.get().getBookInfo().getBookData()).getBaseUrl());
                    }
                    break;
                case JSTOR:
                    url = new GenericUrl("https://www.jstor.org");
                    break;
                default:
                    url = new GenericUrl("http://www.ya.ru");
            }
            HttpConnections.baseUrls.put(urlType, url);
        }
        return url;
    }

    private static HttpResponse _getResponse(Proxy proxy, final UrlType urlType) {
        final GenericUrl baseUrl = HttpConnections.getBaseUrl(urlType);

        try {
            return new NetHttpTransport.Builder().setProxy(proxy).build().createRequestFactory().buildGetRequest(baseUrl).setHeaders(HttpConnections.headers).setConnectTimeout(10000).execute();
        } catch (IOException e) {
            return null;
        }
    }
}
