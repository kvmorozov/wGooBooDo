package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.asynchttpclient.*;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.ListenableFuture.CompletedFailure;
import org.asynchttpclient.channel.ChannelPool;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterContext.FilterContextBuilder;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.netty.channel.ChannelManager;
import org.asynchttpclient.netty.request.NettyRequestSender;
import org.asynchttpclient.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Created by km on 15.01.2017.
 */
public class SingleChannelHttpClient implements AsyncHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncHttpClient.class);
    private final AsyncHttpClientConfig config;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ChannelManager channelManager;
    private final NettyRequestSender requestSender;
    private final boolean allowStopNettyTimer;
    private final Timer nettyTimer;

    /**
     * Default signature calculator to use for all requests constructed by this
     * client instance.
     *
     * @since 1.1
     */
    protected SignatureCalculator signatureCalculator;

    /**
     * Create a new HTTP Asynchronous Client using the default
     * {@link DefaultAsyncHttpClientConfig} configuration. The default
     * {@link AsyncHttpClient} that will be used will be based on the classpath
     * configuration.
     * <p>
     * If none of those providers are found, then the engine will throw an
     * IllegalStateException.
     */
    public SingleChannelHttpClient() {
        this(new Builder().build(), null);
    }

    /**
     * Create a new HTTP Asynchronous Client using the specified
     * {@link DefaultAsyncHttpClientConfig} configuration. This configuration
     * will be passed to the default {@link AsyncHttpClient} that will be
     * selected based on the classpath configuration.
     *
     * @param config a {@link DefaultAsyncHttpClientConfig}
     */
    public SingleChannelHttpClient(final AsyncHttpClientConfig config, final ChannelManager channelManager) {

        this.config = config;

        allowStopNettyTimer = null == config.getNettyTimer();
        nettyTimer = allowStopNettyTimer ? newNettyTimer() : config.getNettyTimer();

        this.channelManager = null == channelManager ? new ChannelManager(config, nettyTimer) : channelManager;
        requestSender = new NettyRequestSender(config, channelManager, nettyTimer, new AsyncHttpClientState(closed));
        this.channelManager.configureBootstraps(requestSender);
    }

    private static Timer newNettyTimer() {
        final HashedWheelTimer timer = new HashedWheelTimer();
        timer.start();
        return timer;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                channelManager.close();
            } catch (final Throwable t) {
                LOGGER.warn("Unexpected error on ChannelManager close", t);
            }
            if (allowStopNettyTimer) {
                try {
                    nettyTimer.stop();
                } catch (final Throwable t) {
                    LOGGER.warn("Unexpected error on HashedWheelTimer close", t);
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public SingleChannelHttpClient setSignatureCalculator(final SignatureCalculator signatureCalculator) {
        this.signatureCalculator = signatureCalculator;
        return this;
    }

    @Override
    public BoundRequestBuilder prepareGet(final String url) {
        return requestBuilder("GET", url);
    }

    @Override
    public BoundRequestBuilder prepareConnect(final String url) {
        return requestBuilder("CONNECT", url);
    }

    @Override
    public BoundRequestBuilder prepareOptions(final String url) {
        return requestBuilder("OPTIONS", url);
    }

    @Override
    public BoundRequestBuilder prepareHead(final String url) {
        return requestBuilder("HEAD", url);
    }

    @Override
    public BoundRequestBuilder preparePost(final String url) {
        return requestBuilder("POST", url);
    }

    @Override
    public BoundRequestBuilder preparePut(final String url) {
        return requestBuilder("PUT", url);
    }

    @Override
    public BoundRequestBuilder prepareDelete(final String url) {
        return requestBuilder("DELETE", url);
    }

    @Override
    public BoundRequestBuilder preparePatch(final String url) {
        return requestBuilder("PATCH", url);
    }

    @Override
    public BoundRequestBuilder prepareTrace(final String url) {
        return requestBuilder("TRACE", url);
    }

    @Override
    public BoundRequestBuilder prepareRequest(final Request request) {
        return requestBuilder(request);
    }

    @Override
    public BoundRequestBuilder prepareRequest(final RequestBuilder requestBuilder) {
        return prepareRequest(requestBuilder.build());
    }

    @Override
    public <T> ListenableFuture<T> executeRequest(final Request request, final org.asynchttpclient.AsyncHandler<T> handler) {

        if (config.getRequestFilters().isEmpty()) {
            return execute(request, handler);

        }
        else {
            FilterContext<T> fc = new FilterContextBuilder<T>().asyncHandler(handler).request(request).build();
            try {
                fc = preProcessRequest(fc);
            } catch (final Exception e) {
                handler.onThrowable(e);
                return new CompletedFailure<>("preProcessRequest failed", e);
            }

            return execute(fc.getRequest(), fc.getAsyncHandler());
        }
    }

    @Override
    public <T> ListenableFuture<T> executeRequest(final RequestBuilder requestBuilder, final org.asynchttpclient.AsyncHandler<T> handler) {
        return executeRequest(requestBuilder.build(), handler);
    }

    @Override
    public ListenableFuture<Response> executeRequest(final Request request) {
        return executeRequest(request, new AsyncCompletionHandlerBase());
    }

    @Override
    public ListenableFuture<Response> executeRequest(final RequestBuilder requestBuilder) {
        return executeRequest(requestBuilder.build());
    }

    private <T> ListenableFuture<T> execute(final Request request, final org.asynchttpclient.AsyncHandler<T> asyncHandler) {
        try {
            return requestSender.sendRequest(request, asyncHandler, null, false);
        } catch (final Exception e) {
            asyncHandler.onThrowable(e);
            return new CompletedFailure<>(e);
        }
    }

    /**
     * Configure and execute the associated {@link RequestFilter}. This class
     * may decorate the {@link Request} and {@link AsyncHandler}
     *
     * @param fc {@link FilterContext}
     * @return {@link FilterContext}
     */
    private <T> FilterContext<T> preProcessRequest(FilterContext<T> fc) throws FilterException {
        for (final RequestFilter asyncFilter : config.getRequestFilters()) {
            fc = asyncFilter.filter(fc);
            Assertions.assertNotNull(fc, "filterContext");
        }

        Request request = fc.getRequest();
        if (fc.getAsyncHandler() instanceof ResumableAsyncHandler) {
            request = ResumableAsyncHandler.class.cast(fc.getAsyncHandler()).adjustRequestRange(request);
        }

        if (0 != request.getRangeOffset()) {
            final RequestBuilder builder = new RequestBuilder(request);
            builder.setHeader("Range", "bytes=" + request.getRangeOffset() + '-');
            request = builder.build();
        }
        fc = new FilterContextBuilder<>(fc).request(request).build();
        return fc;
    }

    public ChannelPool getChannelPool() {
        return channelManager.getChannelPool();
    }

    public EventLoopGroup getEventLoopGroup() {
        return channelManager.getEventLoopGroup();
    }

    @Override
    public ClientStats getClientStats() {
        return channelManager.getClientStats();
    }

    @Override
    public void flushChannelPoolPartitions(final Predicate<Object> predicate) {

    }

    @Override
    public AsyncHttpClientConfig getConfig() {
        return null;
    }

    protected BoundRequestBuilder requestBuilder(final String method, final String url) {
        return new BoundRequestBuilder(this, method, config.isDisableUrlEncodingForBoundRequests()).setUrl(url).setSignatureCalculator(signatureCalculator);
    }

    protected BoundRequestBuilder requestBuilder(final Request prototype) {
        return new BoundRequestBuilder(this, prototype).setSignatureCalculator(signatureCalculator);
    }

}
