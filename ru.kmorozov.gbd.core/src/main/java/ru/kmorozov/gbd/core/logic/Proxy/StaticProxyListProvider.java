package ru.kmorozov.gbd.core.logic.Proxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Created by km on 27.11.2015.
 */
public class StaticProxyListProvider extends AbstractProxyListProvider {

    private static final String PROXY_LIST_RES = "proxy/list1";

    StaticProxyListProvider() {
        this.buildList();
    }

    private void buildList() {
        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(StaticProxyListProvider.PROXY_LIST_RES)) {
            proxyItems = new String(is.readAllBytes(), StandardCharsets.UTF_8).lines().collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {

    }
}
