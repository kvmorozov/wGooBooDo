package ru.kmorozov.gbd.core.logic.connectors.google;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
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

    private HttpRequestFactory getFactory(HttpHostExt proxy) {
        String key = HttpConnector.getProxyKey(proxy);

        HttpRequestFactory requestFactory = GoogleHttpConnector.httpFactoryMap.get(key);

        if (null == requestFactory) synchronized (proxy) {
            requestFactory = new NetHttpTransport.Builder().setProxy(proxy.getProxy()).build().createRequestFactory();

            GoogleHttpConnector.httpFactoryMap.put(key, requestFactory);
        }

        return requestFactory;
    }

    @Override
    public GoogleResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        try {
            GenericUrl url = new GenericUrl(URI.create(rqUrl));

            if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

            HttpResponse resp;
            if (this.validateProxy(rqUrl, proxy)) {
                HttpRequest req = this.getFactory(proxy).buildGetRequest(url)
                        .setConnectTimeout(withTimeout ? HttpConnector.CONNECT_TIMEOUT : HttpConnector.CONNECT_TIMEOUT * 10)
                        .setSuppressUserAgentSuffix(true);
                if (this.needHeaders(rqUrl))
                    req.setHeaders(proxy.getHeaders(this.getUrlType(rqUrl)));

                resp = this.getContent(req, proxy, 0);
            } else throw new RuntimeException("Invalid proxy config!");

            if (null == resp)
                HttpConnector.logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()));

            return new GoogleResponse(resp);
        } catch (HttpResponseException hre) {
            HttpConnector.logger.severe("Connection error: " + hre.getStatusMessage());
            throw new GoogleResponseException(hre);
        }
    }

    private HttpResponse getContent(HttpRequest req, HttpHostExt proxy, int attempt) throws IOException {
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure();
            return null;
        }

        if (1 < attempt) try {
            HttpConnector.logger.finest(String.format("Attempt %d with %s url", attempt, req.getUrl().toString()));
            Thread.sleep((long) (HttpConnector.SLEEP_TIME * attempt));
        } catch (InterruptedException ignored) {
        }

        try {
            return req.execute();
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return this.getContent(req, proxy, ++attempt);
        }
    }

    @Override
    public void close() {

    }
}
