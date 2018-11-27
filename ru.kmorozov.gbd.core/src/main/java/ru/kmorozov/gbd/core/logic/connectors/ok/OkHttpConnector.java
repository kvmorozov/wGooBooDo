package ru.kmorozov.gbd.core.logic.connectors.ok;

import com.google.api.client.http.HttpHeaders;
import okhttp3.Call;
import okhttp3.Call.Factory;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 17.05.2016.
 */
public class OkHttpConnector extends HttpConnector {

    private static final Map<String, OkHttpClient> httpFactoryMap = new ConcurrentHashMap<>();
    private static final Map<String, OkHttpClient> httpFactoryMapWithTimeout = new ConcurrentHashMap<>();

    private Factory getFactory(HttpHostExt proxy, boolean withTimeout) {
        String key = HttpConnector.getProxyKey(proxy);

        Map<String, OkHttpClient> factoryMap = withTimeout ? OkHttpConnector.httpFactoryMapWithTimeout : OkHttpConnector.httpFactoryMap;

        OkHttpClient requestFactory = factoryMap.get(key);

        if (null == requestFactory) synchronized (proxy) {
            requestFactory = new OkHttpClient.Builder().proxy(proxy.getProxy()).connectTimeout((long) (withTimeout ? HttpConnector.CONNECT_TIMEOUT : HttpConnector.CONNECT_TIMEOUT * 10), TimeUnit.MILLISECONDS).build();

            factoryMap.put(key, requestFactory);
        }

        return requestFactory;
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

        HttpHeaders googleHeaders = proxy.getHeaders(this.getUrlType(url));
        List<String> headerItems = new ArrayList<>();
        for (Map.Entry<String, Object> headerItem : googleHeaders.entrySet()) {
            headerItems.add(headerItem.getKey());
            headerItems.add(headerItem.getValue().toString());
        }

        Headers okHeaders = Headers.of(headerItems.toArray(new String[0]));

        Request request = new Request.Builder().url(url).headers(okHeaders).build();
        okhttp3.Response response = this.getContent(request, proxy, withTimeout, 0);

        return new OkResponse(response);
    }

    @Override
    public void close() {

    }


    private okhttp3.Response getContent(Request request, HttpHostExt proxy, boolean withTimeout, int attempt) throws IOException {
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) return null;

        if (0 < attempt) try {
            HttpConnector.logger.finest(String.format("Attempt %d with %s url", attempt, request.url().toString()));
            Thread.sleep((long) (HttpConnector.SLEEP_TIME * attempt));
        } catch (InterruptedException ignored) {
        }

        try {
            return this.getFactory(proxy, withTimeout).newCall(request).execute();
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return this.getContent(request, proxy, withTimeout, ++attempt);
        }
    }
}
