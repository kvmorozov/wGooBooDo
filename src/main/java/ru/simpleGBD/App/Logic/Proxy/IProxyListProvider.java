package ru.simpleGBD.App.Logic.Proxy;

import java.util.Set;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    Set<HttpHostExt> getProxyList();
    void invalidatedProxyListener();
    void updateProxyList();
}
