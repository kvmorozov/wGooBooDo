package ru.kmorozov.gbd.core.logic.Proxy;

import java.util.Optional;

public class ManagedProxyListProvider {

    private final IProxyListProvider parentProvider;
    private final int timeout;

    public ManagedProxyListProvider(final IProxyListProvider parentProvider, final int timeout) {
        this.parentProvider = parentProvider;
        this.timeout = timeout;
    }

    public ManagedProxyListProvider(final int timeout) {
        this(AbstractProxyListProvider.getInstance(), timeout);
    }

    public HttpHostExt getProxy() {
        if (this.checkReady(HttpHostExt.NO_PROXY))
            return HttpHostExt.NO_PROXY;

        if (!this.parentProvider.proxyListCompleted())
            this.parentProvider.processProxyList(UrlType.JSTOR);

        Optional<HttpHostExt> opProxy;
        do {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            opProxy = this.parentProvider
                    .getParallelProxyStream()
                    .filter(this::checkReady)
                    .filter(HttpHostExt::isAvailable)
                    .findFirst();
        }
        while (!opProxy.isPresent());

        return opProxy.get();
    }

    private boolean checkReady(final HttpHostExt proxy) {
        return System.currentTimeMillis() - proxy.getLastUsedTimestamp() > (long) this.timeout;
    }
}
