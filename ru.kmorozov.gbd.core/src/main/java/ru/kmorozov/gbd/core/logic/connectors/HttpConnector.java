package ru.kmorozov.gbd.core.logic.connectors;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.IOException;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 17.05.2016.
 */
public abstract class HttpConnector {

    public static final int CONNECT_TIMEOUT = 30000;
    protected static final Logger logger = INSTANCE.getLogger(HttpConnector.class);
    protected static final int MAX_RETRY_COUNT = 2;
    protected static final int SLEEP_TIME = 500;

    protected String getProxyKey(HttpHostExt proxy) {
        return proxy.toString();
    }

    public abstract Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException;
}
