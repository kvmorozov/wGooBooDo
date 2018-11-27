package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.channel.DefaultChannelPool;
import org.asynchttpclient.netty.ssl.DefaultSslEngineFactory;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyServer.Builder;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by km on 22.12.2016.
 */
public class AsyncHttpConnector extends HttpConnector {

    private final Object BUILDER_LOCK = new Object();
    private final Map<String, AsyncHttpClient> clientsMap = new ConcurrentHashMap<>();
    private volatile DefaultAsyncHttpClientConfig.Builder builder;
    private NioEventLoopGroup nioEventLoopGroup;
    private DefaultChannelPool pool;
    private HashedWheelTimer timer;

    private AsyncHttpClient getClient(final HttpHostExt proxy) {
        final String key = HttpConnector.getProxyKey(proxy);
        AsyncHttpClient client = clientsMap.get(key);

        if (null == builder) synchronized (BUILDER_LOCK) {
            if (null == builder) {
                builder = new DefaultAsyncHttpClientConfig.Builder();
                timer = new HashedWheelTimer();
                timer.start();
                builder.setNettyTimer(timer);
                builder.setThreadFactory(new DefaultThreadFactory("asyncPool"));
                builder.setEventLoopGroup(nioEventLoopGroup = new NioEventLoopGroup());
                builder.setSslEngineFactory(new DefaultSslEngineFactory());

                pool = new DefaultChannelPool(Integer.MAX_VALUE, Integer.MAX_VALUE, timer, Integer.MAX_VALUE);
                builder.setChannelPool(pool);

                builder.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT);
            }
        }

        if (null == client) synchronized (proxy) {
            if (!proxy.isLocal())
                builder.setProxyServer(new Builder(proxy.getHost().getHostName(), proxy.getHost().getPort()).build());
            client = new DefaultAsyncHttpClient(builder.build());

            clientsMap.put(key, client);
        }

        return client;
    }

    @Override
    public Response getContent(final String url, final HttpHostExt proxy, final boolean withTimeout) {
        final AsyncHttpClient client = getClient(proxy);
        final BoundRequestBuilder builder = client.prepareGet(url);
        for (final Entry<String, Object> headerItem : proxy.getHeaders(getUrlType(url)).entrySet())
            if (!"cookie".equals(headerItem.getKey()))
                builder.addHeader(headerItem.getKey(), headerItem.getValue().toString());

        final String[] cookies = proxy.getHeaders(getUrlType(url)).getCookie().split(";");
        for (final String cookieEntry : cookies) {
            final String[] cookieParts = cookieEntry.split("=", 2);
            if (2 != cookieParts.length) continue;

            final Cookie cookie = new DefaultCookie(cookieParts[0], cookieParts[1]);
            cookie.setPath("/");
            cookie.setDomain(".google.ru");
            builder.addCookie(cookie);
        }

        try {
            return new AsyncHttpResponse(builder.execute(new AsyncHandler(proxy)).get((long) HttpConnector.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            return null;
        } finally {
        }
    }

    @Override
    public void close() {
        for (final AsyncHttpClient client : clientsMap.values())
            try {
                if (!client.isClosed())
                    client.close();
            } catch (final IOException ignored) {
            }

        if (null != nioEventLoopGroup)
            nioEventLoopGroup.shutdownGracefully();

        if (null != pool)
            pool.destroy();

        if (null != timer)
            timer.stop();
    }
}
