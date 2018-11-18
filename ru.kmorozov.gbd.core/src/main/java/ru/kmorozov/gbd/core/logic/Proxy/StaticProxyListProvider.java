package ru.kmorozov.gbd.core.logic.Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Created by km on 27.11.2015.
 */
public class StaticProxyListProvider extends AbstractProxyListProvider {

    private static final String PROXY_LIST_RES = "proxy/list1";

    StaticProxyListProvider() {
        buildList();
    }

    private void buildList() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROXY_LIST_RES)) {
            this.proxyItems = new String(is.readAllBytes(), "UTF-8").lines().collect(Collectors.toSet());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {

    }
}
