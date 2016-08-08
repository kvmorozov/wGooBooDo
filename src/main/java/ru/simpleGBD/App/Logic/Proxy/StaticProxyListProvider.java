package ru.simpleGBD.App.Logic.Proxy;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public class StaticProxyListProvider extends AbstractProxyListProvider {

    private static final String PROXY_LIST_RES = "proxy/list1";

    StaticProxyListProvider() {
        buildList();
    }

    private void buildList() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROXY_LIST_RES);) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            this.proxyItems = lines;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {

    }
}
