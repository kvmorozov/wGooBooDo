package ru.kmorozov.gbd.core.logic.Proxy;

public class EmptyProxyListProvider extends AbstractProxyListProvider {

    public static final EmptyProxyListProvider INSTANCE = new EmptyProxyListProvider();

    private EmptyProxyListProvider() {
        proxyList.add(HttpHostExt.NO_PROXY);

        proxyListCompleted.set(true);
    }

    @Override
    public void updateProxyList() {

    }
}
