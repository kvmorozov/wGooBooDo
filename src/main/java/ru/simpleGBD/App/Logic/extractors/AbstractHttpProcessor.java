package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    protected static Logger logger = Logger.getLogger(ExecutionContext.output, AbstractHttpProcessor.class.getName());

    private static int MAX_RETRY_COUNT = 2;
    private static int SLEEP_TIME = 100;

    private static Map<String, HttpRequestFactory> httpFactoryMap = new ConcurrentHashMap<>();

    private HttpRequestFactory getFactory(HttpHostExt proxy) {
        String key = proxy == null ? HttpHostExt.NO_PROXY : proxy.toString();

        HttpRequestFactory requestFactory = httpFactoryMap.get(key);

        if (requestFactory == null)
            synchronized (key) {
                requestFactory = new NetHttpTransport.Builder().
                        setProxy(proxy == null ? Proxy.NO_PROXY : proxy.getProxy()).build().createRequestFactory();

                httpFactoryMap.put(key, requestFactory);
            }

        return requestFactory;
    }

    protected InputStream getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        GenericUrl url = new GenericUrl(rqUrl);

        try {
            return getContent(url, proxy);
        } catch (Exception e) {
            for (int i = 1; i <= MAX_RETRY_COUNT; i++) {
                try {
                    logger.finest(String.format("Try %d get url %s", i, rqUrl));
                    return getContent(url, proxy);
                } catch (Exception ex) {
                    try {
                        Thread.sleep(SLEEP_TIME * i);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        return null;
    }

    private InputStream getContent(GenericUrl url, HttpHostExt proxy) throws IOException {
        HttpResponse response = getFactory(proxy).buildGetRequest(url).setHeaders(HttpConnections.INSTANCE.getHeaders()).execute();

        return response == null ? null : response.getContent();
    }
}
