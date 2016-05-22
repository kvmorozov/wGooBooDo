package ru.simpleGBD.App.Logic.connectors.ok;

import com.google.api.client.http.HttpHeaders;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.connectors.HttpConnector;
import ru.simpleGBD.App.Logic.connectors.Response;
import ru.simpleGBD.App.Utils.HttpConnections;

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

    private static Map<String, OkHttpClient> httpFactoryMap = new ConcurrentHashMap<>();
    private static Map<String, OkHttpClient> httpFactoryMapWithTimeout = new ConcurrentHashMap<>();

    private OkHttpClient getFactory(HttpHostExt proxy, boolean withTimeout) {
        String key = getProxyKey(proxy);

        Map<String, OkHttpClient> factoryMap = withTimeout ? httpFactoryMapWithTimeout : httpFactoryMap;

        OkHttpClient requestFactory = factoryMap.get(key);

        if (requestFactory == null)
            synchronized (key) {
                requestFactory = new OkHttpClient.Builder()
                        .proxy(proxy.getProxy())
                        .connectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10, TimeUnit.MILLISECONDS)
                        .build();

                factoryMap.put(key, requestFactory);
            }

        return requestFactory;
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable())
            return null;

        HttpHeaders googleHeaders = HttpConnections.getHeaders(proxy);
        List<String> headerItems = new ArrayList<>();
        for(Map.Entry<String, Object> headerItem : googleHeaders.entrySet()) {
            headerItems.add(headerItem.getKey());
            headerItems.add(headerItem.getValue().toString());
        }

        Headers okHeaders = Headers.of(headerItems.toArray(new String[headerItems.size()]));

        Request request = new Request.Builder().url(url).headers(okHeaders).build();
        okhttp3.Response response = getContent(request, proxy, withTimeout, 0);

        return new OkResponse(response);
    }


    private okhttp3.Response getContent(Request request, HttpHostExt proxy, boolean withTimeout, int attempt) throws IOException {
        if (attempt >= MAX_RETRY_COUNT)
            return null;

        if (attempt > 0)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, request.url().toString()));
                Thread.sleep(SLEEP_TIME * attempt);
            } catch (InterruptedException ie) {
            }

        try {
            return getFactory(proxy, withTimeout).newCall(request).execute();
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(request, proxy, withTimeout, attempt++);
        }
    }
}
