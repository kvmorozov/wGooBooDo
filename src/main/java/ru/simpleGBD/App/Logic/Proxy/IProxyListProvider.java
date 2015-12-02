package ru.simpleGBD.App.Logic.Proxy;

import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    static IProxyListProvider INSTANCE = new StaticProxyListProvider();

    List<HttpHostExt> getProxyList();

    static IProxyListProvider getInstance() {return INSTANCE;}
}
