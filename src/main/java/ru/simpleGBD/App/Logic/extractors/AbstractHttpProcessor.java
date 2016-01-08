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
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    protected static Logger logger = Logger.getLogger(ExecutionContext.output, AbstractHttpProcessor.class.getName());

    private static int MAX_RETRY_COUNT = 2;
    private static int SLEEP_TIME = 100;
    private static int CONNECT_TIMEOUT = 2000;

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

    protected HttpResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        GenericUrl url = new GenericUrl(rqUrl);

        try {
            return getContent(url, proxy, withTimeout);
        } catch (SocketTimeoutException e) {
            for (int i = 1; i <= MAX_RETRY_COUNT; i++) {
                try {
                    logger.finest(String.format("Try %d get url %s", i, rqUrl));
                    return getContent(url, proxy, withTimeout);
                } catch (Exception ex) {
                    try {
                        Thread.sleep(SLEEP_TIME * i);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        } catch (IOException ioe) {
            if (proxy != null)
                proxy.registerFailure();

            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return null;
    }

    private HttpResponse getContent(GenericUrl url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        return getFactory(proxy).buildGetRequest(url).setConnectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10).setHeaders(HttpConnections.INSTANCE.getHeaders(proxy)).execute();
    }
}
