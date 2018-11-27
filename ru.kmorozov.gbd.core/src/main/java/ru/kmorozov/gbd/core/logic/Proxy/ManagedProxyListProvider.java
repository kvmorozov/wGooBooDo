package ru.kmorozov.gbd.core.logic.Proxy;

import java.util.Optional;

public class ManagedProxyListProvider {

    private final IProxyListProvider parentProvider;
    private final int timeout;

    public ManagedProxyListProvider(IProxyListProvider parentProvider, int timeout) {
        this.parentProvider = parentProvider;
        this.timeout = timeout;
    }

    public ManagedProxyListProvider(int timeout) {
        this(AbstractProxyListProvider.getInstance(), timeout);
    }

    public HttpHostExt getProxy() {
        if (checkReady(HttpHostExt.NO_PROXY))
            return HttpHostExt.NO_PROXY;

        if (!parentProvider.proxyListCompleted())
            parentProvider.processProxyList(UrlType.JSTOR);

        Optional<HttpHostExt> opProxy;
        do {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            opProxy = parentProvider
                    .getParallelProxyStream()
                    .filter(this::checkReady)
                    .filter(HttpHostExt::isAvailable)
                    .findFirst();
        }
        while (!opProxy.isPresent());

        return opProxy.get();
    }

    private boolean checkReady(HttpHostExt proxy) {
        return System.currentTimeMillis() - proxy.getLastUsedTimestamp() > (long) timeout;
    }
}
