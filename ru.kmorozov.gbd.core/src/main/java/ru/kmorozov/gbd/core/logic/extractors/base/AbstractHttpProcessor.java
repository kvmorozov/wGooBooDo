package ru.kmorozov.gbd.core.logic.extractors.base;

import com.google.api.client.http.HttpStatusCodes;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.connectors.ResponseException;
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector;
import ru.kmorozov.gbd.core.utils.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    protected static final Logger logger = INSTANCE.getLogger(AbstractHttpProcessor.class);

    private static final HttpConnector connector = new GoogleHttpConnector();

    protected Response getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        try {
            Response resp = connector.getContent(rqUrl, proxy, withTimeout);
            return proxy.isLocal() ? resp : resp == null ? getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
        } catch (ResponseException re) {
            switch (re.getStatusCode()) {
                case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE:
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
            if (!ioe.getClass().getName().equals(IOException.class.getName())) ioe.printStackTrace();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}