package ru.simpleGBD.App.Logic.connectors.apache;

import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;
import org.apache.hc.core5.http.HttpResponse;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.connectors.HttpConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by km on 17.05.2016.
 */
public class ApacheHttpConnector extends HttpConnector {

    public ApacheResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable())
            return null;

        HttpResponse response = getContent(ApacheConnections.INSTANCE.getClient(proxy, withTimeout), new HttpGet(rqUrl), proxy, 0);

        if (response == null)
            logger.finest(String.format("No response at url %s with proxy %s", rqUrl, proxy.toString()));

        return new ApacheResponse(response);
    }

    private HttpResponse getContent(HttpClient client, HttpGet req, HttpHostExt proxy, int attempt) throws IOException {
        if (attempt >= MAX_RETRY_COUNT)
            return null;

        if (attempt > 0)
            try {
                logger.finest(String.format("Attempt %d with %s url", attempt, req.getURI().toString()));
                Thread.sleep(SLEEP_TIME * attempt);
            } catch (InterruptedException ignored) {
            }

        try {
            return client.execute(req);
        } catch (SocketTimeoutException ste1) {
            proxy.registerFailure();
            return getContent(client, req, proxy, attempt++);
        }
    }
}
