package ru.kmorozov.gbd.core.logic.connectors.apache;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.Proxy.UrlType;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;
import ru.kmorozov.gbd.utils.HttpConnections;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 22.05.2016.
 */
public final class ApacheConnections {

    static final ApacheConnections INSTANCE = new ApacheConnections();
    private final HttpClientBuilder builder;
    private final HttpClientBuilder builderWithTimeout;
    private final Map<HttpHost, HttpClient> clientsMap = new ConcurrentHashMap<>();
    private final Map<HttpHost, HttpClient> withTimeoutClientsMap = new ConcurrentHashMap<>();
    private final Map<HttpHostExt, CookieStore> cookieStoreMap = new ConcurrentHashMap<>();
    private CloseableHttpClient noProxyClient;

    private ApacheConnections() {
        final PoolingHttpClientConnectionManager connPool = new PoolingHttpClientConnectionManager();
        connPool.setMaxTotal(200);
        connPool.setDefaultMaxPerRoute(200);

        builder = HttpClientBuilder.create().setUserAgent(HttpConnections.USER_AGENT).setConnectionManager(connPool).setConnectionManagerShared(true);

        builderWithTimeout = HttpClientBuilder.create().setUserAgent(HttpConnections.USER_AGENT).setConnectionManager(connPool);

        try {
            final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
//            builder.setSSLContext(sslContext);
//            builderWithTimeout.setSSLContext(sslContext);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        final RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                                                         .setConnectTimeout((long) HttpConnector.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                                                         .setConnectionRequestTimeout((long) HttpConnector.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).build();

        builderWithTimeout.setDefaultRequestConfig(requestConfig);
    }

    public HttpClient getClient(final HttpHostExt proxy, final boolean withTimeout) {
        final HttpClientBuilder _builder = withTimeout ? builderWithTimeout : builder;
        _builder.setDefaultCookieStore(getCookieStore(proxy));

        if (proxy.isLocal()) {
            if (null == noProxyClient) noProxyClient = _builder.build();

            return noProxyClient;
        }
        else {
            final Map<HttpHost, HttpClient> _map = withTimeout ? withTimeoutClientsMap : clientsMap;

            final HttpHost host = new HttpHost(proxy.getHost().getAddress());

            return _map.computeIfAbsent(host, k -> _builder.setProxy(host).build());
        }
    }

    public void closeAllConnections() {
        try {
            if (null != noProxyClient) noProxyClient.close();

            if (null != clientsMap) for (final HttpClient client : clientsMap.values())
                ((Closeable) client).close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private CookieStore getCookieStore(final HttpHostExt proxy) {
        CookieStore cookieStore = cookieStoreMap.get(proxy);

        if (null == cookieStore) {
            synchronized (proxy) {
                cookieStore = new BasicCookieStore();
                final String[] cookies = proxy.getHeaders(UrlType.GOOGLE_BOOKS).getCookie().split(";");

                for (final String cookieEntry : cookies) {
                    final String[] cookieParts = cookieEntry.split("=", 2);

                    if (null != cookieParts && 1 < cookieParts.length && LibraryFactory.needSetCookies()) {
                        final SetCookie cookie = new BasicClientCookie(cookieParts[0], cookieParts[1]);
                        cookie.setDomain(".google.ru");
                        cookie.setPath("/");
                        cookieStore.addCookie(cookie);
                    }
                }

                cookieStoreMap.put(proxy, cookieStore);
            }
        }

        return cookieStore;
    }
}
