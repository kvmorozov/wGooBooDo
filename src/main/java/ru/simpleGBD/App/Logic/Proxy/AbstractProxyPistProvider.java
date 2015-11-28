package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by km on 27.11.2015.
 */
public abstract class AbstractProxyPistProvider implements IProxyListProvider {

    private static Logger logger = Logger.getLogger("ProxyPistProvider");

    protected List<HttpHost> proxyList;

    protected void buildFromList(List<String> proxyItems) {
        proxyList = new ArrayList<>(proxyItems.size());
        for (String proxyItem : proxyItems) {
            String[] proxyItemArr = proxyItem.split(":");
            HttpHost host = new HttpHost(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
            if (checkProxy(host)) {
                proxyList.add(host);
                logger.info(String.format("Proxy %s added.", host.toHostString()));
            }
            else
                logger.severe(String.format("Proxy %s NOT added.", host.toHostString()));
        }

        HttpConnections.INSTANCE.initClients(proxyList);
    }

    private boolean checkProxy(HttpHost proxy) {
        try {
            HttpConnections.INSTANCE
                    .getBuilderWithTimeout().setProxy(proxy).build().execute(new HttpGet(ExecutionContext.baseUrl));

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<HttpHost> getProxyList() {
        return proxyList;
    }
}
