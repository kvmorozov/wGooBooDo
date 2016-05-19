package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.http.HttpStatusCodes;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.connectors.HttpConnector;
import ru.simpleGBD.App.Logic.connectors.Response;
import ru.simpleGBD.App.Logic.connectors.ResponseException;
import ru.simpleGBD.App.Logic.connectors.ok.OkHttpConnector;
import ru.simpleGBD.App.Utils.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by km on 05.12.2015.
 */
public class AbstractHttpProcessor {

    protected static Logger logger = Logger.getLogger(ExecutionContext.output, AbstractHttpProcessor.class.getName());

    private static final HttpConnector connector = new OkHttpConnector();

    protected Response getContent(String rqUrl, HttpHostExt proxy, boolean withTimeout) {
        try {
            Response resp = connector.getContent(rqUrl, proxy, withTimeout);
            return proxy.isLocal() ? resp : resp == null ? getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout) : resp;
        } catch (ResponseException re) {
            switch(re.getStatusCode()) {
                case HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE :
                    proxy.forceInvalidate();
                    break;
                case HttpStatusCodes.STATUS_CODE_NOT_FOUND :
                case HttpStatusCodes.STATUS_CODE_BAD_GATEWAY :
                    proxy.registerFailure();
                    break;
                default:
                    re.printStackTrace();
            }

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (SocketException | SSLException se) {
            proxy.registerFailure();
            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        }
        catch (IOException ioe) {
            proxy.registerFailure();

            // Если что-то более специфическое
            if (!ioe.getClass().getName().equals(IOException.class.getName()))
                ioe.printStackTrace();

            return proxy.isLocal() ? null : getContent(rqUrl, HttpHostExt.NO_PROXY, withTimeout);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}