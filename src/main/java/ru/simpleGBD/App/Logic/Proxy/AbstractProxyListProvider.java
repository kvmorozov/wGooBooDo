package ru.simpleGBD.App.Logic.Proxy;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import org.apache.http.HttpHost;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public abstract class AbstractProxyListProvider implements IProxyListProvider {

    protected static final String DEFAULT_PROXY_DELIMITER = ":";

    private static Logger logger = Logger.getLogger(ExecutionContext.output, "ProxyPistProvider");

    private static IProxyListProvider INSTANCE;
    private HttpHeaders headers = new HttpHeaders().setUserAgent(HttpConnections.USER_AGENT);

    protected List<HttpHostExt> proxyList = new ArrayList<>();

    protected void buildFromList(List<String> proxyItems) {
        for (String proxyItem : proxyItems) {
            String[] proxyItemArr = splitItems(proxyItem);

            if (proxyItemArr == null || proxyItemArr.length < 2)
                continue;

            HttpHost host = new HttpHost(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
            String cookie = getCookie(host);
            if (cookie != null) {
                HttpHostExt proxy = new HttpHostExt(host, cookie);

                if ((GBDOptions.secureMode() && proxy.isSecure()) || !GBDOptions.secureMode()) {
                    proxyList.add(proxy);
                    logger.info(String.format("%sroxy %s added.",
                            GBDOptions.secureMode() ? proxy.isSecure() ? "Secure p" : "NOT secure p" : "P",
                            host.toHostString()));
                } else
                    logger.info(String.format("NOT secure proxy %s NOT added.", host.toHostString()));
            } else
                logger.info(String.format("Proxy %s NOT added.", host.toHostString()));
        }
    }

    protected String[] splitItems(String proxyItem, String delimiter) {
        return  proxyItem.split(delimiter);
    }

    protected String[] splitItems(String proxyItem) {
        String[] tmpItems = splitItems(proxyItem, DEFAULT_PROXY_DELIMITER);
        if (tmpItems != null && tmpItems.length >= 2)
            return tmpItems;
        else {
            tmpItems = splitItems(proxyItem, "\\s+");
            return tmpItems != null && tmpItems.length >= 2 ? tmpItems : null;
        }
    }

    private String getCookie(HttpHost proxy) {
        try {
            HttpResponse resp = HttpConnections.getResponse(proxy, headers);

            return ((ArrayList) resp.getHeaders().get("set-cookie")).get(0).toString().split(";")[0];
        } catch (Exception e) {
            return null;
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

    @Override
    public void invalidatedProxyListener() {
        long liveproxyCount = proxyList.stream().filter(p -> p.isAvailable()).count();
        if (liveproxyCount == 0 && GBDOptions.secureMode())
            throw new RuntimeException("No more proxies!");
    }
}
