package ru.simpleGBD.App.Logic.Proxy;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by km on 27.11.2015.
 */
public class StaticProxyListProvider extends AbstractProxyPistProvider {

    private static final String PROXY_LIST_RES = "proxy/list1";

    public static final IProxyListProvider INSTANCE = new StaticProxyListProvider();

    StaticProxyListProvider() {
        buildList();
    }

    private void buildList() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROXY_LIST_RES);
        try {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            buildFromList(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
