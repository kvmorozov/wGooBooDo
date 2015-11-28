package ru.simpleGBD.App.Utils;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;

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

    private static final int HTTP_TIMEOUT = 500;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36";

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

    public void setCookies(Map<String, String> cookiesMap) {
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

    public void initClients(List<HttpHost> proxyList) {
        noProxyClient = builder.build();
        clientsMap = new HashMap<>();

        for (HttpHost proxy : proxyList)
            clientsMap.put(proxy, builder.setProxy(proxy).build());
    }

    public HttpClient getClient(HttpHost proxy) {
        //return proxy == null ? noProxyClient : clientsMap.get(proxy);
        return proxy == null ? builder.build() : builder.setProxy(proxy).build();
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
