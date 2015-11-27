package ru.simpleGBD.App.Utils;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static HttpConnections INSTANCE = new HttpConnections();

    private BasicCookieStore cookieStore = new BasicCookieStore();

    private HttpConnections() {

    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookies(Map<String, String> cookiesMap) {
        for(Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }
}
