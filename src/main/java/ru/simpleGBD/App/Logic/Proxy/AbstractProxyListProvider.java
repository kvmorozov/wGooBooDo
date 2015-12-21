package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public abstract class AbstractProxyListProvider implements IProxyListProvider {

    private static Logger logger = logger = Logger.getLogger(ExecutionContext.output, "ProxyPistProvider");

    private static IProxyListProvider INSTANCE;

    protected List<HttpHostExt> proxyList;

    protected void buildFromList(List<String> proxyItems) {
        proxyList = new ArrayList<>(proxyItems.size());
        for (String proxyItem : proxyItems) {
            String[] proxyItemArr = proxyItem.split(":");

            if (proxyItemArr == null || proxyItemArr.length != 2)
                continue;

            HttpHost host = new HttpHost(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
            if (checkProxy(host)) {
                proxyList.add(new HttpHostExt(host));
                logger.info(String.format("Proxy %s added.", host.toHostString()));
            } else
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
    public List<HttpHostExt> getProxyList() {
        return proxyList;
    }

    public static IProxyListProvider getInstance() {
        if (INSTANCE == null)
            INSTANCE = GBDOptions.getProxyListFile() == null ? new StaticProxyListProvider() : new FileProxyListProvider();

        return INSTANCE;
    }
}