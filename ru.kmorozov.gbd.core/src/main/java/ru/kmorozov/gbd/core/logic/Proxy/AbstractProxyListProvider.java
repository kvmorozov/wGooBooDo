package ru.kmorozov.gbd.core.logic.Proxy;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.web.WebProxyListProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.utils.HttpConnections;
import ru.kmorozov.gbd.utils.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
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

    protected final Collection<HttpHostExt> proxyList = new HashSet<>();
    protected Set<String> proxyItems;
    private final AtomicBoolean proxyListCompleted = new AtomicBoolean(false);

    public static AbstractProxyListProvider getInstance() {
        if (null == INSTANCE) INSTANCE = null == GBDOptions.getProxyListFile() ? new WebProxyListProvider() : new FileProxyListProvider();

        return INSTANCE;
    }

    public static void updateBlacklist() {
        ProxyBlacklistHolder.BLACKLIST.updateBlacklist(INSTANCE.proxyList);
    }

    private static String[] splitItems(final String proxyItem, final String delimiter) {
        return proxyItem.split(delimiter);
    }

    private String[] splitItems(final String proxyItem) {
        String[] tmpItems = splitItems(proxyItem, DEFAULT_PROXY_DELIMITER);
        if (2 <= tmpItems.length) return tmpItems;
        else {
            tmpItems = splitItems(proxyItem, "\\s+");
            return 2 <= tmpItems.length ? tmpItems : null;
        }
    }

    private static String getCookie(final InetSocketAddress proxy) {
        return HttpConnections.getCookieString(proxy);
    }

    public void processProxyList() {
        for (final String proxyString : proxyItems)
            if (!Strings.isNullOrEmpty(proxyString))
                (new Thread(new ProxyChecker(proxyString))).start();
    }

    @Override
    public void invalidatedProxyListener() {
        final long liveProxyCount = proxyList.stream().filter(HttpHostExt::isAvailable).count();
        if (0 == liveProxyCount && GBDOptions.secureMode()) throw new RuntimeException("No more proxies!");
    }

    @Override
    public Stream<HttpHostExt> getParallelProxyStream() {
        if (!proxyListCompleted.get()) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        return proxyList.parallelStream();
    }

    public Iterable<HttpHostExt> getProxyList() {
        return proxyList;
    }

    protected static boolean notBlacklisted(final String proxyStr) {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr);
    }

    public int getProxyCount() {
        return proxyItems.size();
    }

    private class ProxyChecker implements Runnable {

        private final String proxyStr;

        ProxyChecker(final String proxyStr) {
            this.proxyStr = proxyStr;
        }

        @Override
        public void run() {
            final HttpHostExt host = processProxyItem(proxyStr);
            ExecutionContext.INSTANCE.newProxyEvent(host);
        }

        private HttpHostExt processProxyItem(final String proxyItem) {
            HttpHostExt proxy = null;

            try {
                final String[] proxyItemArr = splitItems(proxyItem);

                if (null == proxyItemArr || 2 > proxyItemArr.length) return null;

                final InetSocketAddress host = new InetSocketAddress(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
                final String cookie = getCookie(host);
                proxy = new HttpHostExt(host, cookie);
                if (!StringUtils.isEmpty(cookie)) {
                    if (!GBDOptions.secureMode() || proxy.isSecure()) {
                        logger.info(String.format("%sroxy %s added.", GBDOptions.secureMode() ? proxy.isSecure() ? "Secure p" : "NOT secure p" : "P", host.toString()));
                    }
                    else {
                        logger.info(String.format("NOT secure proxy %s NOT added.", host.toString()));
                        proxy.forceInvalidate(false);
                    }
                }
                else {
                    logger.info(String.format("Proxy %s NOT added.", host.toString()));
                    proxy.forceInvalidate(false);
                }
            } catch (final Exception ex) {
                logger.info(String.format("Not valid proxy string %s.", proxyItem));
            }

            proxyList.add(proxy);
            proxyListCompleted.set(proxyList.size() == proxyItems.size());

            return proxy;
        }
    }
}
