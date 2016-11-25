package ru.kmorozov.gbd.core.logic.connectors.google;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 17.05.2016.
 */
public class GoogleHttpConnector extends HttpConnector {

    private static final Map<String, HttpRequestFactory> httpFactoryMap = new ConcurrentHashMap<>();

    private HttpRequestFactory getFactory(final HttpHostExt proxy) {
        String key = getProxyKey(proxy);

        HttpRequestFactory requestFactory = httpFactoryMap.get(key);

        if (requestFactory == null) synchronized (proxy) {
            requestFactory = new NetHttpTransport.Builder().setProxy(proxy.getProxy()).build().createRequestFactory();

            httpFactoryMap.put(key, requestFactory);
        }

        return requestFactory;
    }

    public GoogleResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        try {
            GenericUrl url = new GenericUrl(rqUrl);

            if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

            HttpResponse resp = null;
            HttpHeaders headers = proxy.getHeaders();
            if (!StringUtils.isEmpty(headers.getCookie()) && headers.getCookie().contains("NID")) {
                HttpRequest req = getFactory(proxy).buildGetRequest(url).setConnectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10).setHeaders(headers);
                resp = getContent(req, proxy, 0);
            }

            if (resp == null)
                logger.finest(String.format("No response at url %s with proxy %s", url.toString(), proxy.toString()));

            return new GoogleResponse(resp);
        } catch (HttpResponseException hre) {
            throw new GoogleResponseException(hre);
        }
    }

    private HttpResponse getContent(HttpRequest req, HttpHostExt proxy, int attempt) throws IOException {
        if (attempt >= MAX_RETRY_COUNT) return null;

        if (attempt > 0) try {
            logger.finest(String.format("Attempt %d with %s url", attempt, req.getUrl().toString()));
            Thread.sleep(SLEEP_TIME * attempt);
        } catch (InterruptedException ignored) {
        }

        try {
            return req.execute();
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(req, proxy, attempt++);
        }
    }
}
