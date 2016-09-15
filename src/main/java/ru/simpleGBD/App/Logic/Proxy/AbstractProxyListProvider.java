package ru.simpleGBD.App.Logic.Proxy;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import org.apache.http.HttpHost;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Utils.HttpConnections;
import ru.simpleGBD.App.Utils.Logger;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by km on 27.11.2015.
 */
@SuppressWarnings("ALL")
public abstract class AbstractProxyListProvider implements IProxyListProvider {

    private static final String DEFAULT_PROXY_DELIMITER = ":";

    private static final Logger logger = Logger.getLogger(ExecutionContext.output, "ProxyPistProvider");

    private static IProxyListProvider INSTANCE;
    private final HttpHeaders headers = new HttpHeaders().setUserAgent(HttpConnections.USER_AGENT);

    final Set<HttpHostExt> proxyList = new HashSet<>();
    private boolean proxyListCompleted = false;
    List<String> proxyItems;

    private HttpHostExt processProxyItem(String proxyItem) {
        HttpHostExt proxy = null;

        try {
            String[] proxyItemArr = splitItems(proxyItem);

            if (proxyItemArr == null || proxyItemArr.length < 2)
                return null;

            HttpHost host = new HttpHost(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
            String cookie = getCookie(host);
            if (cookie != null) {
                proxy = new HttpHostExt(host, cookie);

                if ((GBDOptions.secureMode() && proxy.isSecure()) || !GBDOptions.secureMode()) {
                    proxyList.add(proxy);
                    logger.info(String.format("%sroxy %s added.",
                            GBDOptions.secureMode() ? proxy.isSecure() ? "Secure p" : "NOT secure p" : "P",
                            host.toHostString()));
                } else
                    logger.info(String.format("NOT secure proxy %s NOT added.", host.toHostString()));
            } else
                logger.info(String.format("Proxy %s NOT added.", host.toHostString()));
        } catch (Exception ex) {
            logger.info(String.format("Not valid proxy string %s.", proxyItem));
        }

        return proxy;
    }

    private String[] splitItems(String proxyItem, String delimiter) {
        return proxyItem.split(delimiter);
    }

    private String[] splitItems(String proxyItem) {
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

    class InternalProxyIterator implements Iterator<HttpHostExt> {

        private final Iterator<String> itr;

        private InternalProxyIterator() {
            itr = proxyItems.iterator();

            proxyListCompleted = true;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public HttpHostExt next() {
            return processProxyItem(itr.next());
        }
    }

    @Override
    public Iterator<HttpHostExt> getProxyList() {
        return proxyListCompleted ? proxyList.iterator() : new InternalProxyIterator();
    }

    public static IProxyListProvider getInstance() {
        if (INSTANCE == null)
            INSTANCE = GBDOptions.getProxyListFile() == null ? new StaticProxyListProvider() : new WebProxyListProvider();

        return INSTANCE;
    }

    @Override
    public void invalidatedProxyListener() {
        long liveProxyCount = proxyList.stream().filter(p -> p.isAvailable()).count();
        if (liveProxyCount == 0 && GBDOptions.secureMode())
            throw new RuntimeException("No more proxies!");
    }

    @Override
    public Stream<HttpHostExt> getParallelProxyStream() {
        Iterator<HttpHostExt> hostIterator = getProxyList();
        Iterable<HttpHostExt> iterable = () -> hostIterator;
        return StreamSupport.stream(iterable.spliterator(), true);
    }
}
