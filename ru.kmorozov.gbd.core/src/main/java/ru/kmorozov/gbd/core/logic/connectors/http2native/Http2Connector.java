package ru.kmorozov.gbd.core.logic.connectors.http2native;

import com.google.api.client.http.HttpHeaders;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Http2Connector extends HttpConnector {

    private static final Map<String, HttpClient> httpClientsMap = new ConcurrentHashMap<>();

    private static final HttpClient DEFAULT_CLIENT = HttpClient.newHttpClient();

    private HttpClient getClient(final HttpHostExt proxy) {
        final String key = HttpConnector.getProxyKey(proxy);

        HttpClient requestClient = httpClientsMap.get(key);

        if (null == requestClient) synchronized (proxy) {
            requestClient = HttpClient.newBuilder().proxy(ProxySelector.of(proxy.getHost())).build();

            httpClientsMap.put(key, requestClient);
        }

        return requestClient;
    }

    @Override
    public Http2Response getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        try {
            final URI uri = URI.create(rqUrl);

            if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

            final HttpResponse resp;
            if (validateProxy(rqUrl, proxy)) {
                final HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                        .uri(uri).GET().timeout(Duration.ofMillis((long) (withTimeout ? HttpConnector.CONNECT_TIMEOUT : HttpConnector.CONNECT_TIMEOUT * 10)));

                if (needHeaders(rqUrl)) {
                    HttpHeaders headers = proxy.getHeaders(getUrlType(rqUrl));
                    reqBuilder.setHeader("User-Agent", headers.getUserAgent());
                    reqBuilder.setHeader("Cookie", headers.getCookie());
                }

                resp = getContent(reqBuilder.build(), proxy, 0);
            } else throw new RuntimeException("Invalid proxy config!");

            if (null == resp)
                HttpConnector.logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()));

            return new Http2Response(resp);
        } catch (final IOException ioe) {
            HttpConnector.logger.severe("Connection error: " + ioe.getMessage());
            throw new Http2ResponseException(ioe);
        }
    }

    private HttpResponse getContent(final HttpRequest req, final HttpHostExt proxy, int attempt) throws IOException {
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) {
            proxy.registerFailure();
            return null;
        }

        if (1 < attempt) try {
            HttpConnector.logger.finest(String.format("Attempt %d with %s url", attempt, req.uri().toString()));
            Thread.sleep((long) (HttpConnector.SLEEP_TIME * attempt));
        } catch (final InterruptedException ignored) {
        }

        try {
            return DEFAULT_CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());
        } catch (final SocketTimeoutException | InterruptedException ste1) {
            proxy.registerFailure();
            return getContent(req, proxy, ++attempt);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
