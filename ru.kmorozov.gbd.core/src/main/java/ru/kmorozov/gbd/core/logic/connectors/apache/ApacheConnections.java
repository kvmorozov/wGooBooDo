package ru.kmorozov.gbd.core.logic.connectors.apache;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.sync.HttpClient;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.utils.HttpConnections;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.kmorozov.gbd.core.logic.connectors.HttpConnector.CONNECT_TIMEOUT;
import static ru.kmorozov.gbd.core.utils.HttpConnections.USER_AGENT;

/**
 * Created by km on 22.05.2016.
 */
public class ApacheConnections {

    private final HttpClientBuilder builder;
    private final HttpClientBuilder builderWithTimeout;
    private CloseableHttpClient noProxyClient;
    private final Map<HttpHost, HttpClient> clientsMap = new ConcurrentHashMap<>();
    private final Map<HttpHost, HttpClient> withTimeoutClientsMap = new ConcurrentHashMap<>();

    public static final ApacheConnections INSTANCE = new ApacheConnections();

    private ApacheConnections() {
        BasicCookieStore cookieStore = new BasicCookieStore();

        StringBuilder cookieBuilder = new StringBuilder();

        for (Map.Entry<String, String> cookieEntry : HttpConnections.getCookiesMap().entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(".google.ru");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);

            cookieBuilder.append(cookieEntry.getKey()).append("=").append(cookieEntry.getValue()).append("; ");
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
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
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

            HttpHost host = new HttpHost(proxy.getHost().getAddress());
            HttpClient client = _map.get(host);
            if (client == null) {
                client = _builder.setProxy(host).build();
                _map.put(host, client);
            }

            return client;
        }
    }

    public void closeAllConnections() {
        try {
            if (noProxyClient != null)
                noProxyClient.close();

            if (clientsMap != null)
                for (HttpClient client : clientsMap.values())
                    ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
