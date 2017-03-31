package ru.kmorozov.gbd.client;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.utils.Logger;
import ru.kmorozov.gbd.core.utils.gson.Mapper;

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

    protected String getRestServiceBaseUrl() {
        return DEFAULT_REST_SERVICE_URL;
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
        try (Socket socket = new Socket()) {
            URL serviceURL = new URL(getRestServiceBaseUrl());
            socket.connect(new InetSocketAddress(serviceURL.getHost(), serviceURL.getPort()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getRawResult(String rqUrl) throws RestServiceUnavailableException {
        try {
            Response resp = getContent(rqUrl, HttpHostExt.NO_PROXY, true);
            if (resp == null || resp.getContent() == null) {
                logger.info("Rest service is unavailable!");
                throw new RestServiceUnavailableException();
            }

            try (InputStream is = resp.getContent()) {
                return IOUtils.toString(is, Charset.defaultCharset());
            } catch (SocketException | SSLException se) {
                logger.info("Rest service is unavailable! " + se.getMessage());
                throw new RestServiceUnavailableException();
            }
        } catch (IOException ioe) {
            logger.info("Rest service is unavailable! " + ioe.getMessage());
            throw new RestServiceUnavailableException();
        }
    }

    protected <T> T getCallResult(String operation, Class<T> resultClass, RestParam... parameters) {
        StringBuilder rqUrl = new StringBuilder(getRestServiceBaseUrl() + operation);

        if (parameters != null && parameters.length > 0) {
            rqUrl.append("?");
            for (RestParam param : parameters)
                rqUrl.append(param.paramName).append("=").append(param.value.toString()).append("&");
        }

        String rawResult;

        try {
            rawResult = getRawResult(rqUrl.toString());
        } catch (RestServiceUnavailableException e) {
            logger.finest(String.format("Service %s call failed!", operation));
            return resultClass.equals(Boolean.class) ? (T) Boolean.FALSE : null;
        }

        if (StringUtils.isEmpty(rawResult)) {
            logger.finest(String.format("Service %s call failed!", operation));
            return null;
        }

        return Mapper.getGson().fromJson(rawResult, resultClass);
    }
}
