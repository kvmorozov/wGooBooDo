package ru.simpleGBD.App.Logic.connectors;

import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;

/**
 * Created by km on 17.05.2016.
 */
public abstract class HttpConnector {

    protected static Logger logger = Logger.getLogger(ExecutionContext.output, HttpConnector.class.getName());

    protected static int MAX_RETRY_COUNT = 2;
    protected static int SLEEP_TIME = 500;
    public static int CONNECT_TIMEOUT = 2000;

    protected String getProxyKey(HttpHostExt proxy) {
        return proxy.toString();
    }

    public abstract Response getContent(String url, HttpHostExt proxy, boolean withTimeout) throws IOException;
}
