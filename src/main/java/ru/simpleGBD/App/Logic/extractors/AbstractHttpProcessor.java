package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
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

    private static int MAX_RETRY_COUNT = 2;
    private static int SLEEP_TIME = 500;
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

    protected HttpResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        GenericUrl url = new GenericUrl(rqUrl);

        try {
            HttpResponse resp = getContent(url, proxy, withTimeout);
            return proxy.isLocal() ? resp : resp == null ? getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
        } catch (IOException ioe) {
            proxy.registerFailure();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private HttpResponse getContent(GenericUrl url, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable())
            return null;

        HttpRequest req = getFactory(proxy).buildGetRequest(url)
                .setConnectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10)
                .setHeaders(HttpConnections.getHeaders(proxy));
        HttpResponse resp = getContent(req, proxy, 0);

        if (resp == null)
            logger.finest(String.format("No response at url %s with proxy %s", url.toString(), proxy.toString()));

        return resp;
    }

    private HttpResponse getContent(HttpRequest req, HttpHostExt proxy, int attempt) throws IOException {
        if (attempt > 0)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.getUrl().toString()));
                Thread.sleep(SLEEP_TIME * attempt);
            } catch (InterruptedException ie) {
            }

        try {
            return req.execute();
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(req, proxy, attempt++);
        }
    }
}