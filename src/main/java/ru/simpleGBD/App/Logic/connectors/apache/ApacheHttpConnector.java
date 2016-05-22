package ru.simpleGBD.App.Logic.connectors.apache;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.connectors.HttpConnector;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by km on 17.05.2016.
 */
public class ApacheHttpConnector extends HttpConnector {

    private static Map<String, HttpClient> httpFactoryMap = new ConcurrentHashMap<>();

    public ApacheResponse getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) throws IOException {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable())
            return null;

        HttpResponse response = ApacheConnections.INSTANCE.getClient(proxy, withTimeout).execute(new HttpGet(rqUrl));

        if (response == null)
            logger.finest(String.format("No response at url %s with proxy %s", rqUrl.toString(), proxy.toString()));

        return new ApacheResponse(response);
    }
}
