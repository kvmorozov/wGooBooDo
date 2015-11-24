package ru.kmorozov.App.Utils;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    public static HttpConnections INSTANCE = new HttpConnections();

    private BasicCookieStore cookieStore = new BasicCookieStore();

    private HttpConnections() {
        initCookies();
    }

    private void initCookies() {
        BasicClientCookie cookie = new BasicClientCookie("APISID", "QIuXJzZ3J2i0NlpK/A85tyWXpqZRzBtm_r");
        cookie.setDomain(".google.ru");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);

        cookie = new BasicClientCookie("HSID", "A9gE5VmfOaq6y2Cjl");
        cookie.setDomain(".google.ru");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);

        cookie = new BasicClientCookie("NID", "73=F7u5NJGBYulJuIAKbWh3NVz8p2PVcHtnj3B24cEPH_tCqlsvfPpDM7Ht75mukGPTP53N0UtANd_D0pUTCKqXvH6ZgZ3zwTEWL1So7tO8xOgnCk1Uh-rS3qeC9d4u2ycN7vD9SN0W0BPeF264Qd_CEq4MMs0MYAGX7wMWTI0gzdtZK7rXaPJhavUsqj64DFWzD8Qw-_pfxbEIJaH3hs5eOwdkc7B0wcYs2izKb20lt9ftrBD2MfgjA4DBWBQJLzBLFQ1yymJSabv7k52BrCPgA_vggg");
        cookie.setDomain(".google.ru");
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }
}
