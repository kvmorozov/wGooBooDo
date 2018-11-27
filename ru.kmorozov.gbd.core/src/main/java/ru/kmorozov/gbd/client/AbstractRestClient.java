package ru.kmorozov.gbd.client;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.db.utils.Mapper;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.logger.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by km on 20.12.2016.
 */
public abstract class AbstractRestClient extends AbstractHttpProcessor {

    protected static final Logger logger = Logger.getLogger(AbstractRestClient.class);

    private static final String DEFAULT_REST_SERVICE_URL = "http://localhost:8080/";

    protected static String getRestServiceBaseUrl() {
        return AbstractRestClient.DEFAULT_REST_SERVICE_URL;
    }

    protected static final class RestParam {

        String paramName;
        Object value;

        RestParam(String paramName, Object value) {
            this.paramName = paramName;
            this.value = value;
        }
    }

    public boolean serviceAvailable() {
        try (final Socket socket = new Socket()) {
            URL serviceURL = new URL(AbstractRestClient.DEFAULT_REST_SERVICE_URL);
            socket.connect(new InetSocketAddress(serviceURL.getHost(), serviceURL.getPort()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getRawResult(String rqUrl) throws RestServiceUnavailableException {
        try {
            Response resp = AbstractHttpProcessor.getContent(rqUrl, HttpHostExt.NO_PROXY, true);
            if (null == resp || null == resp.getContent()) {
                AbstractRestClient.logger.info("Rest service is unavailable!");
                throw new RestServiceUnavailableException();
            }

            try (final InputStream is = resp.getContent()) {
                return new String(is.readAllBytes(), Charset.defaultCharset());
            } catch (final SocketException | SSLException se) {
                AbstractRestClient.logger.info("Rest service is unavailable! " + se.getMessage());
                throw new RestServiceUnavailableException();
            }
        } catch (IOException ioe) {
            AbstractRestClient.logger.info("Rest service is unavailable! " + ioe.getMessage());
            throw new RestServiceUnavailableException();
        }
    }

    protected <T> T getCallResult(String operation, Class<T> resultClass, AbstractRestClient.RestParam... parameters) {
        StringBuilder rqUrl = new StringBuilder(AbstractRestClient.DEFAULT_REST_SERVICE_URL + operation);

        if (null != parameters && 0 < parameters.length) {
            rqUrl.append('?');
            for (AbstractRestClient.RestParam param : parameters)
                rqUrl.append(param.paramName).append('=').append(param.value).append('&');
        }

        String rawResult;

        try {
            rawResult = this.getRawResult(rqUrl.toString());
        } catch (RestServiceUnavailableException e) {
            AbstractRestClient.logger.finest(String.format("Service %s call failed!", operation));
            return resultClass.equals(Boolean.class) ? (T) Boolean.FALSE : null;
        }

        if (StringUtils.isEmpty(rawResult)) {
            AbstractRestClient.logger.finest(String.format("Service %s call failed!", operation));
            return null;
        }

        return Mapper.getGson().fromJson(rawResult, resultClass);
    }
}
