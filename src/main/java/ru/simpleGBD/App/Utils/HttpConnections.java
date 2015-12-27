package ru.simpleGBD.App.Utils;

import com.google.api.client.http.HttpHeaders;

import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    private static final int HTTP_TIMEOUT = 2000;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    public static HttpConnections INSTANCE = new HttpConnections();

    private String cookieString;
    private HttpHeaders headers;

    private HttpConnections() {
    }

    public void setDefaultCookies(Map<String, String> cookiesMap) {
        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            cookieBuilder.append(cookieEntry.getKey() + "=" + cookieEntry.getValue() + "; ");
        }

        cookieString = cookieBuilder.toString();
    }

    public HttpHeaders getHeaders() {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.setUserAgent(USER_AGENT);
//            headers.setCookie(cookieString);
//            headers.set("Referer", "https://books.google.ru/books?id=BEvEV9OVzacC&printsec=frontcover&hl=ru&redir_esc=y");
//            headers.set("Proxy-Authorization", "475453b13f6162d37f933a2ae9468823d00a288854a11d138c2a02f4f1cc0970fbd7c79fb02230c6");
        }

        return headers;
    }

    public String getCookieString() {
        return cookieString;
    }
}
