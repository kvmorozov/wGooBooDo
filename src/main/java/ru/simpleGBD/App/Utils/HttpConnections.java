package ru.simpleGBD.App.Utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    private static final int HTTP_TIMEOUT = 500;

    public static HttpConnections INSTANCE = new HttpConnections();

    private BasicCookieStore cookieStore = new BasicCookieStore();
    private HttpClientBuilder builder;

    private HttpConnections() {

    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookies(Map<String, String> cookiesMap) {
        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }

    public HttpClientBuilder getBuilder() {
        if (builder != null)
            return builder;

        builder = HttpClientBuilder
                .create()
                .setUserAgent(ImageExtractor.USER_AGENT)
                .setDefaultCookieStore(HttpConnections.INSTANCE.getCookieStore());

        try {
            // setup a Trust Strategy that allows all certificates.
            //
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            builder.setSSLContext(sslContext);

            RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                    .setSocketTimeout(HTTP_TIMEOUT)
                    .setConnectTimeout(HTTP_TIMEOUT)
                    .setConnectionRequestTimeout(HTTP_TIMEOUT)
                    .build();

            builder.setDefaultRequestConfig(requestConfig);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return builder;
    }
}
