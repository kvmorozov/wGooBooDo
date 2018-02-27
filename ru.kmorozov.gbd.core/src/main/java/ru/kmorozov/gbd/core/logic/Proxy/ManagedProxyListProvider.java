package ru.kmorozov.gbd.core.logic.Proxy;

import java.util.Optional;

public class ManagedProxyListProvider {

    private IProxyListProvider parentProvider;
    private int timeout;

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

        Optional<HttpHostExt> opProxy = null;
        do {
            try {
                Thread.sleep(100);
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
        return System.currentTimeMillis() - proxy.getLastUsedTimestamp() > timeout;
    }
}
