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
        if (null == AbstractHttpProcessor.connectors || AbstractHttpProcessor.connectors.isEmpty()) synchronized (AbstractHttpProcessor.LOCK) {
            if (null == AbstractHttpProcessor.connectors || AbstractHttpProcessor.connectors.isEmpty())
                AbstractHttpProcessor.connectors = LibraryFactory.preferredConnectors();
        }

        return AbstractHttpProcessor.connectors;
    }

    protected static Response getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        try {
            Response resp = null;
            for (HttpConnector connector : AbstractHttpProcessor.getConnectors()) {
                resp = connector.getContent(rqUrl, proxy, withTimeout);
                resp = proxy.isLocal() ? resp : null == resp ? AbstractHttpProcessor.getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
                if (null != resp)
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
                    proxy.forceInvalidate(true);
                    break;
                default:
                    re.printStackTrace();
            }

            return proxy.isLocal() ? null : AbstractHttpProcessor.getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (final SocketException | SSLException se) {
            proxy.registerFailure();
            return proxy.isLocal() ? null : AbstractHttpProcessor.getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (IOException ioe) {
            proxy.registerFailure();

            // Если что-то более специфическое
            if (!ioe.getClass().equals(IOException.class)) ioe.printStackTrace();

            return proxy.isLocal() ? null : AbstractHttpProcessor.getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void close() {
        for (HttpConnector connector : AbstractHttpProcessor.getConnectors()) {
            try {
                connector.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}