package ru.simpleGBD.App.Logic.Proxy;

import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    List<HttpHostExt> getProxyList();
    void invalidatedProxyListener();
    void updateProxyList();
}
