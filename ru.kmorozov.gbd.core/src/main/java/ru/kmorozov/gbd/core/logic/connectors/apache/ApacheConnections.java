package ru.kmorozov.gbd.core.logic.connectors.apache;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.sync.HttpClient;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;

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

    static final ApacheConnections INSTANCE = new ApacheConnections();
    private final HttpClientBuilder builder;
    private final HttpClientBuilder builderWithTimeout;
    private final Map<HttpHost, HttpClient> clientsMap = new ConcurrentHashMap<>();
    private final Map<HttpHost, HttpClient> withTimeoutClientsMap = new ConcurrentHashMap<>();
    private final Map<HttpHostExt, CookieStore> cookieStoreMap = new ConcurrentHashMap<>();
    private CloseableHttpClient noProxyClient;

    private ApacheConnections() {
        PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(200);
        connPool.setDefaultMaxPerRoute(200);

        builder = HttpClientBuilder.create().setUserAgent(USER_AGENT).setConnectionManager(connPool).setConnectionManagerShared(true);

        builderWithTimeout = HttpClientBuilder.create().setUserAgent(USER_AGENT).setConnectionManager(connPool);

        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
//            builder.setSSLContext(sslContext);
//            builderWithTimeout.setSSLContext(sslContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                                                   .setSocketTimeout(TimeValue.ofMillis(CONNECT_TIMEOUT))
                                                   .setConnectTimeout(TimeValue.ofMillis(CONNECT_TIMEOUT))
                                                   .setConnectionRequestTimeout(TimeValue.ofMillis(CONNECT_TIMEOUT)).build();

        builderWithTimeout.setDefaultRequestConfig(requestConfig);
    }

    public HttpClient getClient(HttpHostExt proxy, boolean withTimeout) {
        HttpClientBuilder _builder = withTimeout ? builderWithTimeout : builder;
        _builder.setDefaultCookieStore(getCookieStore(proxy));

        if (proxy.isLocal()) {
            if (noProxyClient == null) noProxyClient = _builder.build();

            return noProxyClient;
        }
        else {
            Map<HttpHost, HttpClient> _map = withTimeout ? withTimeoutClientsMap : clientsMap;

            HttpHost host = new HttpHost(proxy.getHost().getAddress());

            return _map.computeIfAbsent(host, k -> _builder.setProxy(host).build());
        }
    }

    public void closeAllConnections() {
        try {
            if (noProxyClient != null) noProxyClient.close();

            if (clientsMap != null) for (HttpClient client : clientsMap.values())
                ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CookieStore getCookieStore(HttpHostExt proxy) {
        CookieStore cookieStore = cookieStoreMap.get(proxy);

        if (cookieStore == null) {
            synchronized (proxy) {
                cookieStore = new BasicCookieStore();
                String[] cookies = proxy.getHeaders().getCookie().split(";");

                for (String cookieEntry : cookies) {
                    String[] cookieParts = cookieEntry.split("=", 2);

                    BasicClientCookie cookie = new BasicClientCookie(cookieParts[0], cookieParts[1]);
                    cookie.setDomain(".google.ru");
                    cookie.setPath("/");
                    cookieStore.addCookie(cookie);
                }

                cookieStoreMap.put(proxy, cookieStore);
            }
        }

        return cookieStore;
    }
}
