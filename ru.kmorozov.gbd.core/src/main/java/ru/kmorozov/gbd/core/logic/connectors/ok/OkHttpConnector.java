package ru.kmorozov.gbd.core.logic.connectors.ok;

import com.google.api.client.http.HttpHeaders;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 17.05.2016.
 */
public class OkHttpConnector extends HttpConnector {

    private static final Map<String, OkHttpClient> httpFactoryMap = new ConcurrentHashMap<>();
    private static final Map<String, OkHttpClient> httpFactoryMapWithTimeout = new ConcurrentHashMap<>();

    private Call.Factory getFactory(final HttpHostExt proxy, final boolean withTimeout) {
        final String key = HttpConnector.getProxyKey(proxy);

        final Map<String, OkHttpClient> factoryMap = withTimeout ? httpFactoryMapWithTimeout : httpFactoryMap;

        OkHttpClient requestFactory = factoryMap.get(key);

        if (null == requestFactory) synchronized (proxy) {
            requestFactory = new OkHttpClient.Builder().proxy(proxy.getProxy()).connectTimeout((long) (withTimeout ? HttpConnector.CONNECT_TIMEOUT : HttpConnector.CONNECT_TIMEOUT * 10), TimeUnit.MILLISECONDS).build();

            factoryMap.put(key, requestFactory);
        }

        return requestFactory;
    }

    @Override
    public Response getContent(final String url, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

        final HttpHeaders googleHeaders = proxy.getHeaders(getUrlType(url));
        final List<String> headerItems = new ArrayList<>();
        for (final Entry<String, Object> headerItem : googleHeaders.entrySet()) {
            headerItems.add(headerItem.getKey());
            headerItems.add(headerItem.getValue().toString());
        }

        final Headers okHeaders = Headers.of(headerItems.toArray(new String[0]));

        final Request request = new Builder().url(url).headers(okHeaders).build();
        final okhttp3.Response response = getContent(request, proxy, withTimeout, 0);

        return new OkResponse(response);
    }

    @Override
    public void close() {

    }


    private okhttp3.Response getContent(final Request request, final HttpHostExt proxy, final boolean withTimeout, int attempt) throws IOException {
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) return null;

        if (0 < attempt) try {
            HttpConnector.logger.finest(String.format("Attempt %d with %s url", attempt, request.url().toString()));
            Thread.sleep((long) (HttpConnector.SLEEP_TIME * attempt));
        } catch (final InterruptedException ignored) {
        }

        try {
            return getFactory(proxy, withTimeout).newCall(request).execute();
        } catch (final SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(request, proxy, withTimeout, ++attempt);
        }
    }
}
