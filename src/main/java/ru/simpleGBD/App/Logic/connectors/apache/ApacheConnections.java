package ru.simpleGBD.App.Logic.connectors.apache;

import org.apache.http.HttpHeaders;
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
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.HttpConnections;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.simpleGBD.App.Logic.connectors.HttpConnector.CONNECT_TIMEOUT;
import static ru.simpleGBD.App.Utils.HttpConnections.USER_AGENT;

/**
 * Created by km on 22.05.2016.
 */
public class ApacheConnections {

    private BasicCookieStore cookieStore;
    private HttpClientBuilder builder, builderWithTimeout;
    private HttpClient noProxyClient;
    private Map<HttpHost, HttpClient> clientsMap = new ConcurrentHashMap<>(), withTimeoutClientsMap = new ConcurrentHashMap<>();

    public static ApacheConnections INSTANCE = new ApacheConnections();

    private ApacheConnections() {
        cookieStore = new BasicCookieStore();

        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : HttpConnections.getCookiesMap().entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);

            cookieBuilder.append(cookieEntry.getKey() + "=" + cookieEntry.getValue() + "; ");
        }

        PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(200);
        connPool.setDefaultMaxPerRoute(200);

        builder = HttpClientBuilder
                .create()
                .setUserAgent(USER_AGENT)
                .setConnectionManager(connPool)
                .setConnectionManagerShared(true)
                .setDefaultCookieStore(cookieStore);

        builderWithTimeout = HttpClientBuilder
                .create()
                .setUserAgent(USER_AGENT)
                .setConnectionManager(connPool)
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
                .setSocketTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .build();

        builderWithTimeout.setDefaultRequestConfig(requestConfig);
    }

    public HttpClient getClient(HttpHostExt proxy, boolean withTimeout) {
        HttpClientBuilder _builder = withTimeout ? builderWithTimeout : builder;

        if (proxy.isLocal()) {
            if (noProxyClient == null)
                noProxyClient = _builder.build();

            return noProxyClient;
        } else {
            Map<HttpHost, HttpClient> _map = withTimeout ? withTimeoutClientsMap : clientsMap;

            if (!_map.containsKey(proxy.getHost())) {
                HttpClient client = _builder.setProxy(proxy.getHost()).build();
                _map.put(proxy.getHost(), client);
            }

            return _map.get(proxy.getHost());
        }
    }

    public void closeAllConnections() {
        try {
            if (noProxyClient != null)
                ((CloseableHttpClient) noProxyClient).close();

            if (clientsMap != null)
                for (HttpClient client : clientsMap.values())
                    ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
