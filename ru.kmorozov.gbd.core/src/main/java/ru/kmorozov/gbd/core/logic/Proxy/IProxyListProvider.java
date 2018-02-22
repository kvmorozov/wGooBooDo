package ru.kmorozov.gbd.core.logic.Proxy;

import java.util.stream.Stream;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    void invalidatedProxyListener();

    void updateProxyList();

    Stream<HttpHostExt> getParallelProxyStream();

    int getProxyCount();

    void processProxyList(UrlType urlType);

    Iterable<HttpHostExt> getProxyList();

    boolean proxyListCompleted();
}
