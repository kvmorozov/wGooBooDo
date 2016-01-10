package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    protected static Logger logger = Logger.getLogger(ExecutionContext.output, AbstractHttpProcessor.class.getName());

    private static int MAX_RETRY_COUNT = 3;
    private static int SLEEP_TIME = 1000;
    private static int CONNECT_TIMEOUT = 2000;

    private static Map<String, HttpRequestFactory> httpFactoryMap = new ConcurrentHashMap<>();

    private String getProxyKey(HttpHostExt proxy) {
        return proxy.toString();
    }

    private HttpRequestFactory getFactory(HttpHostExt proxy) {
        String key = getProxyKey(proxy);

        HttpRequestFactory requestFactory = httpFactoryMap.get(key);

        if (requestFactory == null)
            synchronized (key) {
                requestFactory = new NetHttpTransport.Builder().
                        setProxy(proxy.getProxy()).build().createRequestFactory();

                httpFactoryMap.put(key, requestFactory);
            }

        return requestFactory;
    }

    private void resetFactory(HttpHostExt proxy) {
        httpFactoryMap.remove(getProxyKey(proxy));
    }

    protected HttpResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        GenericUrl url = new GenericUrl(rqUrl);

        try {
            return getContent(url, proxy, withTimeout);
        } catch (SocketTimeoutException ste1) {
            for (int i = 1; i <= MAX_RETRY_COUNT; i++) {
                try {
                    resetFactory(proxy);
                    logger.finest(String.format("Try %d get url %s with proxy %s", i, rqUrl, proxy.toString()));
                    return getContent(url, proxy, withTimeout);
                } catch (SocketTimeoutException ste2) {
                    try {
                        Thread.sleep(SLEEP_TIME * i);
                    } catch (InterruptedException ie) {
                    }
                }
            }

            proxy.registerFailure();
        } catch (IOException ioe) {
            if (!proxy.isLocal())
                proxy.registerFailure();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
    }

    private HttpResponse getContent(GenericUrl url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if (GBDOptions.secureMode() && proxy.isLocal())
            return null;

        HttpResponse resp = getFactory(proxy).buildGetRequest(url)
                .setConnectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10)
                .setHeaders(HttpConnections.INSTANCE.getHeaders(proxy)).execute();

        return resp;
    }
}
