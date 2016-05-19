package ru.simpleGBD.App.Utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.HttpHost;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    private static HttpConnections INSTANCE = new HttpConnections();

    private HttpHeaders headers;

    private HttpConnections() {
    }

    private void _setDefaultCookies(Map<String, String> cookiesMap) {
        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey() + "=" + cookieEntry.getValue() + "; ");
        }

        HttpHostExt.NO_PROXY.setCookie(cookieBuilder.toString());
    }

    private HttpHeaders _getHeaders(HttpHostExt proxy) {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.setUserAgent(USER_AGENT);
            headers.setCookie(proxy.getCookie());
        }

        return headers;
    }

    private HttpResponse _getResponse(Proxy proxy, HttpHeaders _headers) {
        try {
            return new NetHttpTransport.Builder().
                    setProxy(proxy).
                    build().createRequestFactory().buildGetRequest(new GenericUrl(ExecutionContext.baseUrl)).
                    setHeaders(_headers).execute();
        } catch (IOException e) {
            return null;
        }
    }

    public static Proxy hostToProxy(HttpHost host) {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host.getHostName(), host.getPort()));
    }

    public static HttpResponse getResponse(HttpHost host, HttpHeaders _headers) {
        return INSTANCE._getResponse(hostToProxy(host), _headers);
    }

    public static HttpHeaders getHeaders(HttpHostExt proxy) {
        return INSTANCE._getHeaders(proxy);
    }

    public static void setDefaultCookies(Map<String, String> cookiesMap) {
        INSTANCE._setDefaultCookies(cookiesMap);
    }
}
