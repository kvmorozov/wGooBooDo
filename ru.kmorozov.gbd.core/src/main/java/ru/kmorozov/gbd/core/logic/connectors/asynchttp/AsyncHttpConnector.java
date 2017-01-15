package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.channel.DefaultEventLoop;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.channel.ChannelManager;
import org.asynchttpclient.netty.channel.DefaultChannelPool;
import org.asynchttpclient.netty.ssl.DefaultSslEngineFactory;
import org.asynchttpclient.proxy.ProxyServer;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by km on 22.12.2016.
 */
public class AsyncHttpConnector extends HttpConnector {

    private static final Object BUILDER_LOCK = new Object();
    private static final Map<String, AsyncHttpClient> clientsMap = new ConcurrentHashMap<>();
    private static volatile DefaultAsyncHttpClientConfig.Builder builder;
    private static volatile ChannelManager channelManager;

    private AsyncHttpClient getClient(final HttpHostExt proxy) {
        String key = getProxyKey(proxy);
        AsyncHttpClient client = clientsMap.get(key);

        if (builder == null) synchronized (BUILDER_LOCK) {
            if (builder == null) {
                builder = new DefaultAsyncHttpClientConfig.Builder();
                HashedWheelTimer timer = new HashedWheelTimer();
                timer.start();
                builder.setNettyTimer(timer);
                builder.setThreadFactory(new DefaultThreadFactory("asyncPool"));
                builder.setEventLoopGroup(new DefaultEventLoop());
                builder.setSslEngineFactory(new DefaultSslEngineFactory());

                DefaultChannelPool pool = new DefaultChannelPool(Integer.MAX_VALUE, Integer.MAX_VALUE, timer, Integer.MAX_VALUE);
                builder.setChannelPool(pool);

                builder.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT);

                channelManager = new ChannelManager(builder.build(), timer);
            }
        }

        if (client == null) synchronized (proxy) {
            if (!proxy.isLocal()) builder.setProxyServer(new ProxyServer.Builder(proxy.getHost().getHostName(), proxy.getHost().getPort()).build());
            client = new SingleChannelHttpClient(builder.build(), channelManager);

            clientsMap.put(key, client);
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

            Cookie cookie = new DefaultCookie(cookieParts[0], cookieParts[1]);
            cookie.setPath("/");
            cookie.setDomain(".google.ru");
            builder.addCookie(cookie);
        }

        try {
            return new AsyncHttpResponse(builder.execute(new AsyncHandler(proxy)).get(HttpConnector.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            return null;
        } finally {
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
