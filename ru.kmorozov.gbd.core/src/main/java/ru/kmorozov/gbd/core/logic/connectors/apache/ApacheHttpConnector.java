package ru.kmorozov.gbd.core.logic.connectors.apache;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by km on 17.05.2016.
 */
public class ApacheHttpConnector extends HttpConnector {

    public ApacheResponse getContent(final String rqUrl, final HttpHostExt proxy, final boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return null;

        final HttpResponse response = getContent(ApacheConnections.INSTANCE.getClient(proxy, withTimeout), new HttpGet(rqUrl), proxy, 0);

        if (null == response) HttpConnector.logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()));

        return new ApacheResponse((CloseableHttpResponse) response);
    }

    private static HttpResponse getContent(final HttpClient client, final HttpGet req, final HttpHostExt proxy, int attempt) {
        if (HttpConnector.MAX_RETRY_COUNT <= attempt) return null;

        if (0 < attempt) try {
            HttpConnector.logger.finest(String.format("Attempt %d with %s url", attempt, req.getRequestUri()));
            Thread.sleep((long) (HttpConnector.SLEEP_TIME * attempt));
        } catch (final InterruptedException ignored) {
        }

        try {
            return client.execute(req);
        } catch (final SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(client, req, proxy, attempt++);
        } catch (final Exception ex) {
            proxy.registerFailure();
            return null;
        }
    }

    @Override
    public void close() {
        ApacheConnections.INSTANCE.closeAllConnections();
    }
}
