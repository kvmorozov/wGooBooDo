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

    private final Object BUILDER_LOCK = new Object();
    private final Map<String, AsyncHttpClient> clientsMap = new ConcurrentHashMap<>();
    private volatile DefaultAsyncHttpClientConfig.Builder builder;
    private NioEventLoopGroup nioEventLoopGroup;
    private DefaultChannelPool pool;
    private HashedWheelTimer timer;

    private AsyncHttpClient getClient(HttpHostExt proxy) {
        String key = HttpConnector.getProxyKey(proxy);
        AsyncHttpClient client = this.clientsMap.get(key);

        if (null == this.builder) synchronized (this.BUILDER_LOCK) {
            if (null == this.builder) {
                this.builder = new DefaultAsyncHttpClientConfig.Builder();
                this.timer = new HashedWheelTimer();
                this.timer.start();
                this.builder.setNettyTimer(this.timer);
                this.builder.setThreadFactory(new DefaultThreadFactory("asyncPool"));
                this.builder.setEventLoopGroup(this.nioEventLoopGroup = new NioEventLoopGroup());
                this.builder.setSslEngineFactory(new DefaultSslEngineFactory());

                this.pool = new DefaultChannelPool(Integer.MAX_VALUE, Integer.MAX_VALUE, this.timer, Integer.MAX_VALUE);
                this.builder.setChannelPool(this.pool);

                this.builder.setConnectTimeout(HttpConnector.CONNECT_TIMEOUT);
            }
        }

        if (null == client) synchronized (proxy) {
            if (!proxy.isLocal())
                this.builder.setProxyServer(new ProxyServer.Builder(proxy.getHost().getHostName(), proxy.getHost().getPort()).build());
            client = new DefaultAsyncHttpClient(this.builder.build());

            this.clientsMap.put(key, client);
        }

        return client;
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) {
        AsyncHttpClient client = this.getClient(proxy);
        BoundRequestBuilder builder = client.prepareGet(url);
        for (Map.Entry<String, Object> headerItem : proxy.getHeaders(this.getUrlType(url)).entrySet())
            if (!"cookie".equals(headerItem.getKey()))
                builder.addHeader(headerItem.getKey(), headerItem.getValue().toString());

        String[] cookies = proxy.getHeaders(this.getUrlType(url)).getCookie().split(";");
        for (String cookieEntry : cookies) {
            String[] cookieParts = cookieEntry.split("=", 2);
            if (2 != cookieParts.length) continue;

            Cookie cookie = new DefaultCookie(cookieParts[0], cookieParts[1]);
            cookie.setPath("/");
            cookie.setDomain(".google.ru");
            builder.addCookie(cookie);
        }

        try {
            return new AsyncHttpResponse(builder.execute(new AsyncHandler(proxy)).get((long) HttpConnector.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
            return null;
        } finally {
        }
    }

    @Override
    public void close() {
        for (AsyncHttpClient client : this.clientsMap.values())
            try {
                if (!client.isClosed())
                    client.close();
            } catch (IOException ignored) {
            }

        if (null != this.nioEventLoopGroup)
            this.nioEventLoopGroup.shutdownGracefully();

        if (null != this.pool)
            this.pool.destroy();

        if (null != this.timer)
            this.timer.stop();
    }
}
