package ru.kmorozov.gbd.core.logic.extractors.base;

import com.google.api.client.http.HttpStatusCodes;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.connectors.ResponseException;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.core.utils.ClassUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    private static List<HttpConnector> connectors;
    private static final Object LOCK = new Object();

    private static List<HttpConnector> getConnectors() {
        if (connectors == null || connectors.size() == 0) synchronized (LOCK) {
            if (connectors == null || connectors.size() == 0) {
                connectors = new ArrayList<>();
//                if (ClassUtils.isClassExists("org.asynchttpclient.AsyncHttpClient")) connectors.add(new AsyncHttpConnector());
//                if (ClassUtils.isClassExists("okhttp3.OkHttpClient")) connectors.add(new OkHttpConnector());
//                if (ClassUtils.isClassExists("org.apache.hc.client5.http.sync.HttpClient")) connectors.add(new ApacheHttpConnector());
                if (ClassUtils.isClassExists("com.google.api.client.http.HttpRequestFactory")) connectors.add(new GoogleHttpConnector());
            }
        }

        return connectors;
    }

    protected Response getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        try {
            Response resp = null;
            for (HttpConnector connector : getConnectors()) {
                resp = connector.getContent(rqUrl, proxy, withTimeout);
                resp = proxy.isLocal() ? resp : resp == null ? getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
                if (resp != null)
                    return resp;
            }

            return resp;
        } catch (ResponseException re) {
            switch (re.getStatusCode()) {
                case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE:
                case 413:
                    proxy.forceInvalidate(true);
                    break;
                case HttpStatusCodes.STATUS_CODE_NOT_FOUND:
                case HttpStatusCodes.STATUS_CODE_BAD_GATEWAY:
                    proxy.registerFailure();
                    break;
                default:
                    re.printStackTrace();
            }

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (SocketException | SSLException se) {
            proxy.registerFailure();
            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (IOException ioe) {
            proxy.registerFailure();

            // Если что-то более специфическое
            if (!ioe.getClass().equals(IOException.class)) ioe.printStackTrace();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void close() {
        for (HttpConnector connector : getConnectors())
            connector.close();
    }
}