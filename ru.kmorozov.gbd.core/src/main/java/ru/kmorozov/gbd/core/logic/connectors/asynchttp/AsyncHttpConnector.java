package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.proxy.ProxyServer;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by km on 22.12.2016.
 */
public class AsyncHttpConnector extends HttpConnector {

    private static final Map<String, AsyncHttpClient> clientsMap = new ConcurrentHashMap<>();

    private AsyncHttpClient getClient(final HttpHostExt proxy) {
        String key = getProxyKey(proxy);
        AsyncHttpClient client = clientsMap.get(key);

        if (client == null) synchronized (proxy) {
            DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();
            if (!proxy.isLocal()) builder.setProxyServer(new ProxyServer.Builder(proxy.getHost().getHostName(), proxy.getHost().getPort()).build());
            builder.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT);
            client = new DefaultAsyncHttpClient(builder.build());

//            clientsMap.put(key, client);
        }

        return client;
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        AsyncHttpClient client = getClient(proxy);
        BoundRequestBuilder builder = client.prepareGet(url);
        for (Map.Entry<String, Object> headerItem : proxy.getHeaders().entrySet())
            if (!headerItem.getKey().equals("cookie")) builder.addHeader(headerItem.getKey(), headerItem.getValue().toString());

        String[] cookies = proxy.getHeaders().getCookie().split(";");
        for (String cookieEntry : cookies) {
            String[] cookieParts = cookieEntry.split("=", 2);
            if (cookieParts.length != 2) continue;

            Cookie cookie = new Cookie(cookieParts[0], cookieParts[1], false, ".google.ru", "/", Long.MAX_VALUE, true, true);
            builder.addCookie(cookie);
        }

        try {
            return new AsyncHttpResponse(builder.execute(new AsyncHandler(proxy)).get());
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            client.close();
        }
    }

    @Override
    public void close() {
        for (AsyncHttpClient client : clientsMap.values())
            try {
                if (!client.isClosed())
                    client.close();
            } catch (IOException ignored) {
            }
    }
}
