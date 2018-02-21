package ru.kmorozov.gbd.core.logic.connectors.google;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 17.05.2016.
 */
public class GoogleHttpConnector extends HttpConnector {

    private static final Map<String, HttpRequestFactory> httpFactoryMap = new ConcurrentHashMap<>();

    private HttpRequestFactory getFactory(final HttpHostExt proxy) {
        final String key = getProxyKey(proxy);

        HttpRequestFactory requestFactory = httpFactoryMap.get(key);

        if (null == requestFactory) synchronized (proxy) {
            requestFactory = new Builder().setProxy(proxy.getProxy()).build().createRequestFactory();

            httpFactoryMap.put(key, requestFactory);
        }

        return requestFactory;
    }

    public GoogleResponse getContent(final String rqUrl, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        try {
            final GenericUrl url = new GenericUrl(URI.create(rqUrl));

            if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

            final HttpResponse resp;
            if (validateProxy(rqUrl, proxy)) {
                final HttpRequest req = getFactory(proxy).buildGetRequest(url).setConnectTimeout(withTimeout ? CONNECT_TIMEOUT : CONNECT_TIMEOUT * 10);
                if (needHeaders(rqUrl))
                    req.setHeaders(proxy.getHeaders(getUrlType(rqUrl)));

                resp = getContent(req, proxy, 0);
            } else throw new RuntimeException("Invalid proxy config!");

            if (null == resp)
                logger.finest(String.format("No response at url %s with proxy %s", url.toString(), proxy.toString()));

            return new GoogleResponse(resp);
        } catch (final HttpResponseException hre) {
            logger.severe("Connection error: " + hre.getMessage());
            throw new GoogleResponseException(hre);
        }
    }

    private static HttpResponse getContent(final HttpRequest req, final HttpHostExt proxy, int attempt) throws IOException {
        if (MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure();
            return null;
        }

        if (1 < attempt) try {
            logger.finest(String.format("Attempt %d with %s url", attempt, req.getUrl().toString()));
            Thread.sleep(SLEEP_TIME * attempt);
        } catch (final InterruptedException ignored) {
        }

        try {
            return req.execute();
        } catch (final SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(req, proxy, ++attempt);
        }
    }

    @Override
    public void close() {

    }
}
