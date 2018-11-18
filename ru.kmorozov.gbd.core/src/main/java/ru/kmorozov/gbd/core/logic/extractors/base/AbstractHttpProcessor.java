package ru.kmorozov.gbd.core.logic.extractors.base;

import com.google.api.client.http.HttpStatusCodes;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.connectors.ResponseException;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

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

    private static Iterable<HttpConnector> getConnectors() {
        if (null == connectors || connectors.isEmpty()) synchronized (LOCK) {
            if (null == connectors || connectors.isEmpty())
                connectors = LibraryFactory.preferredConnectors();
        }

        return connectors;
    }

    protected static Response getContent(final String rqUrl, final HttpHostExt proxy, final boolean withTimeout) {
        try {
            Response resp = null;
            for (final HttpConnector connector : getConnectors()) {
                resp = connector.getContent(rqUrl, proxy, withTimeout);
                resp = proxy.isLocal() ? resp : null == resp ? getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
                if (null != resp)
                    return resp;
            }

            return resp;
        } catch (final ResponseException re) {
            switch (re.getStatusCode()) {
                case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE:
                case 413:
                    proxy.forceInvalidate(true);
                    break;
                case HttpStatusCodes.STATUS_CODE_NOT_FOUND:
                case HttpStatusCodes.STATUS_CODE_BAD_GATEWAY:
                    proxy.forceInvalidate(true);
                    break;
                default:
                    re.printStackTrace();
            }

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (SocketException | SSLException se) {
            proxy.registerFailure();
            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (final IOException ioe) {
            proxy.registerFailure();

            // Если что-то более специфическое
            if (!ioe.getClass().equals(IOException.class)) ioe.printStackTrace();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void close() {
        for (final HttpConnector connector : getConnectors()) {
            try {
                connector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}