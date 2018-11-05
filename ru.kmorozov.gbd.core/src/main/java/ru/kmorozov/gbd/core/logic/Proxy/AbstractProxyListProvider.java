package ru.kmorozov.gbd.core.logic.Proxy;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.web.WebProxyListProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.HttpConnections;

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

    private static final Logger logger = Logger.getLogger(AbstractProxyListProvider.class);

    private static AbstractProxyListProvider INSTANCE;

    protected final Collection<HttpHostExt> proxyList = new HashSet<>();
    protected Set<String> proxyItems;

    protected final AtomicBoolean proxyListCompleted = new AtomicBoolean(false);
    private final AtomicBoolean proxyListInitStarted = new AtomicBoolean(false);

    public static IProxyListProvider getInstance() {
        if (null == INSTANCE)
            INSTANCE = null == GBDOptions.getProxyListFile() ? new WebProxyListProvider() : new FileProxyListProvider();

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

    @Override
    public void processProxyList(UrlType urlType) {
        if (proxyListCompleted.get() || proxyListInitStarted.get())
            return;

        proxyListInitStarted.set(true);

        for (final String proxyString : proxyItems)
            if (!Strings.isNullOrEmpty(proxyString))
                (new Thread(new ProxyChecker(proxyString, urlType))).start();
    }

    @Override
    public boolean proxyListCompleted() {
        return proxyListCompleted.get();
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

    @Override
    public Iterable<HttpHostExt> getProxyList() {
        return proxyList;
    }

    protected static boolean notBlacklisted(final String proxyStr) {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr);
    }

    @Override
    public int getProxyCount() {
        return proxyItems.size();
    }

    private class ProxyChecker implements Runnable {

        private final String proxyStr;
        private final UrlType urlType;

        ProxyChecker(final String proxyStr, UrlType urlType) {
            this.proxyStr = proxyStr;
            this.urlType = urlType;
        }

        @Override
        public void run() {
            final HttpHostExt host = processProxyItem(proxyStr);
            ExecutionContext.sendProxyEvent(host);
        }

        private String getCookie(final InetSocketAddress proxy) {
            return HttpConnections.getCookieString(proxy, urlType);
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
                    } else {
                        logger.info(String.format("NOT secure proxy %s NOT added.", host.toString()));
                        proxy.forceInvalidate(false);
                    }
                } else {
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
