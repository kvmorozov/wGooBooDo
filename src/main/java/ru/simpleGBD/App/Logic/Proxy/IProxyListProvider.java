package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;

import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    static IProxyListProvider INSTANCE = new StaticProxyListProvider();

    List<HttpHost> getProxyList();

    static IProxyListProvider getInstance() {return INSTANCE;}
}