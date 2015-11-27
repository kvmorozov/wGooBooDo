package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;

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
            HttpHost host = new HttpHost(proxyItemArr[0], Integer.valueOf(proxyItemArr[1]));
            try {
                if (host.getAddress().isReachable(100)) {
                    proxyList.add(host);
                    logger.info(String.format("Proxy %s added.", host.toHostString()));
                }
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public List<HttpHost> getProxyList() {
        return proxyList;
    }
}
