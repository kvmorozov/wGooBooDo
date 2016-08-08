package ru.simpleGBD.App.Logic.Proxy;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by km on 27.11.2015.
 */
public interface IProxyListProvider {

    Iterator<HttpHostExt> getProxyList();

    void invalidatedProxyListener();

    void updateProxyList();

    Stream<HttpHostExt> getParallelProxyStream();
}
