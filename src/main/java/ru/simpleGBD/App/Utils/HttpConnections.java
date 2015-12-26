package ru.simpleGBD.App.Utils;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by km on 22.11.2015.
 */
public class HttpConnections {

    private static final int HTTP_TIMEOUT = 2000;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    public static HttpConnections INSTANCE = new HttpConnections();

    private BasicCookieStore cookieStore;
    private HttpClientBuilder builder, builderWithTimeout;
    private HttpClient noProxyClient;
    private Map<HttpHost, HttpClient> clientsMap;

    private HttpConnections() {
        cookieStore = new BasicCookieStore();
/*        PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(200);
        connPool.setDefaultMaxPerRoute(200);*/

        builder = HttpClientBuilder
                .create()
                .setUserAgent(USER_AGENT)
//                .setConnectionManager(connPool)
                .setConnectionManagerShared(true)
                .setDefaultCookieStore(cookieStore);

        builderWithTimeout = HttpClientBuilder
                .create()
                .setUserAgent(USER_AGENT)
                .setDefaultCookieStore(cookieStore);

        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            builder.setSSLContext(sslContext);
            builderWithTimeout.setSSLContext(sslContext);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setSocketTimeout(HTTP_TIMEOUT)
                .setConnectTimeout(HTTP_TIMEOUT)
                .setConnectionRequestTimeout(HTTP_TIMEOUT)
                .build();

        builderWithTimeout.setDefaultRequestConfig(requestConfig);
    }

    public void setDefaultCookies(Map<String, String> cookiesMap) {
        for (Map.Entry<String, String> cookieEntry : cookiesMap.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }

    public HttpClientBuilder getBuilderWithTimeout() {
        return builderWithTimeout;
    }

    public void initClients(List<HttpHostExt> proxyList) {
        noProxyClient = builder.build();
        clientsMap = new HashMap<>();

        for (HttpHostExt proxy : proxyList)
            clientsMap.put(proxy.getHost(), builder.setProxy(proxy.getHost()).build());
    }

    public void closeAllConnections() {
        try {
            ((CloseableHttpClient) noProxyClient).close();
            for (HttpClient client : clientsMap.values())
                ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
