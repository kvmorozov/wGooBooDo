package ru.simpleGBD.App.Utils;

import com.google.api.client.http.HttpHeaders;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;

import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    private static final int HTTP_TIMEOUT = 2000;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    public static HttpConnections INSTANCE = new HttpConnections();

    // Default cookie for NO_PROXY
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

    public HttpHeaders getHeaders(HttpHostExt proxy) {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.setUserAgent(USER_AGENT);
            headers.setCookie(proxy == null ? cookieString : proxy.getCookie());
        }

        return headers;
    }
}
