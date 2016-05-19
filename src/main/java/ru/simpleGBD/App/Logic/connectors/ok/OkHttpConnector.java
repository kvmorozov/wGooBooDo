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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 17.05.2016.
 */
public class OkHttpConnector extends HttpConnector {

    private static Map<String, OkHttpClient> httpFactoryMap = new ConcurrentHashMap<>();

    private OkHttpClient getFactory(HttpHostExt proxy) {
        String key = getProxyKey(proxy);

        OkHttpClient requestFactory = httpFactoryMap.get(key);

        if (requestFactory == null)
            synchronized (key) {
                requestFactory = new OkHttpClient.Builder().proxy(proxy.getProxy()).build();

                httpFactoryMap.put(key, requestFactory);
            }

        return requestFactory;
    }

    @Override
    public Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable())
            return null;

        HttpHeaders googleHeader = HttpConnections.getHeaders(proxy);
        List<String> headerItems = new ArrayList<>();
        for(Map.Entry<String, Object> headerItem : googleHeader.entrySet()) {
            headerItems.add(headerItem.getKey());
            headerItems.add(headerItem.getValue().toString());
        }

        Headers okHeaders = Headers.of(headerItems.toArray(new String[headerItems.size()]));

        Request request = new Request.Builder().url(url).headers(okHeaders).build();
        okhttp3.Response response = getFactory(proxy).newCall(request).execute();
//        HttpResponse resp = getContent(req, proxy, 0);

        return new OkResponse(response);
    }
}
