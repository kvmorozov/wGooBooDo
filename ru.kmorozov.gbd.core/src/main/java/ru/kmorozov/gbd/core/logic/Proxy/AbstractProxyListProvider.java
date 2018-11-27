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
        if (null == AbstractProxyListProvider.INSTANCE)
            AbstractProxyListProvider.INSTANCE = null == GBDOptions.getProxyListFile() ? new WebProxyListProvider() : new FileProxyListProvider();

        return AbstractProxyListProvider.INSTANCE;
    }

    public static void updateBlacklist() {
        ProxyBlacklistHolder.BLACKLIST.updateBlacklist(AbstractProxyListProvider.INSTANCE.proxyList);
    }

    private static String[] splitItems(String proxyItem, String delimiter) {
        return proxyItem.split(delimiter);
    }

    private String[] splitItems(String proxyItem) {
        String[] tmpItems = AbstractProxyListProvider.splitItems(proxyItem, AbstractProxyListProvider.DEFAULT_PROXY_DELIMITER);
        if (2 <= tmpItems.length) return tmpItems;
        else {
            tmpItems = AbstractProxyListProvider.splitItems(proxyItem, "\\s+");
            return 2 <= tmpItems.length ? tmpItems : null;
        }
    }

    @Override
    public void processProxyList(final UrlType urlType) {
        if (this.proxyListCompleted.get() || this.proxyListInitStarted.get())
            return;

        this.proxyListInitStarted.set(true);

        for (String proxyString : this.proxyItems)
            if (!Strings.isNullOrEmpty(proxyString))
                (new Thread(new ProxyChecker(proxyString, urlType))).start();
    }

    @Override
    public boolean proxyListCompleted() {
        return this.proxyListCompleted.get();
    }

    @Override
    public void invalidatedProxyListener() {
        long liveProxyCount = this.proxyList.stream().filter(HttpHostExt::isAvailable).count();
        if (0L == liveProxyCount && GBDOptions.secureMode()) throw new RuntimeException("No more proxies!");
    }

    @Override
    public Stream<HttpHostExt> getParallelProxyStream() {
        if (!this.proxyListCompleted.get()) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.proxyList.parallelStream();
    }

    @Override
    public Iterable<HttpHostExt> getProxyList() {
        return this.proxyList;
    }

    protected static boolean notBlacklisted(String proxyStr) {
        return !ProxyBlacklistHolder.BLACKLIST.isProxyInBlacklist(proxyStr);
    }

    @Override
    public int getProxyCount() {
        return this.proxyItems.size();
    }

    private class ProxyChecker implements Runnable {

        private final String proxyStr;
        private final UrlType urlType;

        ProxyChecker(String proxyStr, final UrlType urlType) {
            this.proxyStr = proxyStr;
            this.urlType = urlType;
        }

        @Override
        public void run() {
            HttpHostExt host = this.processProxyItem(this.proxyStr);
            ExecutionContext.sendProxyEvent(host);
        }

        private String getCookie(InetSocketAddress proxy) {
            return HttpConnections.getCookieString(proxy, this.urlType);
        }

        private HttpHostExt processProxyItem(String proxyItem) {
            HttpHostExt proxy = null;

            try {
                String[] proxyItemArr = AbstractProxyListProvider.this.splitItems(proxyItem);

                if (null == proxyItemArr || 2 > proxyItemArr.length) return null;

                InetSocketAddress host = new InetSocketAddress(proxyItemArr[0], Integer.parseInt(proxyItemArr[1]));
                String cookie = this.getCookie(host);
                proxy = new HttpHostExt(host, cookie);
                if (!StringUtils.isEmpty(cookie)) {
                    if (!GBDOptions.secureMode() || proxy.isSecure()) {
                        AbstractProxyListProvider.logger.info(String.format("%sroxy %s added.", GBDOptions.secureMode() ? proxy.isSecure() ? "Secure p" : "NOT secure p" : "P", host.toString()));
                    } else {
                        AbstractProxyListProvider.logger.info(String.format("NOT secure proxy %s NOT added.", host.toString()));
                        proxy.forceInvalidate(false);
                    }
                } else {
                    AbstractProxyListProvider.logger.info(String.format("Proxy %s NOT added.", host.toString()));
                    proxy.forceInvalidate(false);
                }
            } catch (Exception ex) {
                AbstractProxyListProvider.logger.info(String.format("Not valid proxy string %s.", proxyItem));
            }

            AbstractProxyListProvider.this.proxyList.add(proxy);
            AbstractProxyListProvider.this.proxyListCompleted.set(AbstractProxyListProvider.this.proxyList.size() == AbstractProxyListProvider.this.proxyItems.size());

            return proxy;
        }
    }
}
