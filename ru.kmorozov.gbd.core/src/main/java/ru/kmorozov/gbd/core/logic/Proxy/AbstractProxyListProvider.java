package ru.kmorozov.gbd.core.logic.Proxy;

import com.google.api.client.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.utils.HttpConnections;
import ru.kmorozov.gbd.core.utils.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Created by km on 27.11.2015.
 */

public abstract class AbstractProxyListProvider implements IProxyListProvider {

    private static final String DEFAULT_PROXY_DELIMITER = ":";

    private static final Logger logger = ExecutionContext.INSTANCE.getLogger("ProxyListProvider");

    private static AbstractProxyListProvider INSTANCE;

    protected final Set<HttpHostExt> proxyList = new HashSet<>();
    private AtomicBoolean proxyListCompleted = new AtomicBoolean(false);
    protected List<String> proxyItems;

    private String[] splitItems(String proxyItem, String delimiter) {
        return proxyItem.split(delimiter);
    }

    private String[] splitItems(String proxyItem) {
        String[] tmpItems = splitItems(proxyItem, DEFAULT_PROXY_DELIMITER);
        if (tmpItems != null && tmpItems.length >= 2) return tmpItems;
        else {
            tmpItems = splitItems(proxyItem, "\\s+");
            return tmpItems != null && tmpItems.length >= 2 ? tmpItems : null;
        }
    }

    private String getCookie(InetSocketAddress proxy) {
        return HttpConnections.getCookieString(proxy);
    }

    private class ProxyChecker implements Runnable {

        private String proxyStr;

        ProxyChecker(String proxyStr) {
            this.proxyStr = proxyStr;
        }

        @Override
        public void run() {
            HttpHostExt host = processProxyItem(proxyStr);
            ExecutionContext.INSTANCE.newProxyEvent(host);
        }

        private HttpHostExt processProxyItem(String proxyItem) {
            HttpHostExt proxy = null;

            try {
                String[] proxyItemArr = splitItems(proxyItem);

                if (proxyItemArr == null || proxyItemArr.length < 2) return null;

                InetSocketAddress host = new InetSocketAddress(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
                String cookie = getCookie(host);
                proxy = new HttpHostExt(host, cookie);
                if (!StringUtils.isEmpty(cookie)) {
                    if (!GBDOptions.secureMode() || proxy.isSecure()) {
                        logger.info(String.format("%sroxy %s added.", GBDOptions.secureMode() ? proxy.isSecure() ? "Secure p" : "NOT secure p" : "P", host.toString()));
                    } else {
                        logger.info(String.format("NOT secure proxy %s NOT added.", host.toString()));
                        proxy.forceInvalidate(false);
                    }
                } else {
                    logger.info(String.format("Proxy %s NOT added.", host.toString()));
                    proxy.forceInvalidate(false);
                }
            } catch (Exception ex) {
                logger.info(String.format("Not valid proxy string %s.", proxyItem));
            }

            proxyList.add(proxy);
            proxyListCompleted.set(proxyList.size() == proxyItems.size());

            return proxy;
        }
    }

    public void processProxyList() {
        for (String proxyString : proxyItems)
            (new Thread(new ProxyChecker(proxyString))).start();
    }

    public static AbstractProxyListProvider getInstance() {
        if (INSTANCE == null)
            INSTANCE = GBDOptions.getProxyListFile() == null ? new WebProxyListProvider() : new FileProxyListProvider();

        return INSTANCE;
    }

    @Override
    public void invalidatedProxyListener() {
        long liveProxyCount = proxyList.stream().filter(HttpHostExt::isAvailable).count();
        if (liveProxyCount == 0 && GBDOptions.secureMode()) throw new RuntimeException("No more proxies!");
    }

    @Override
    public Stream<HttpHostExt> getParallelProxyStream() {
        if (!proxyListCompleted.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return proxyList.parallelStream();
    }

    public Set<HttpHostExt> getProxyList() {
        return proxyList;
    }

    protected boolean notBlacklisted(String proxyStr) {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr);
    }

    public static void updateBlacklist() {
        ProxyBlacklistHolder.BLACKLIST.updateBlacklist(INSTANCE.proxyList);
    }

    public int getProxyCount() {
        return proxyItems.size();
    }
}
