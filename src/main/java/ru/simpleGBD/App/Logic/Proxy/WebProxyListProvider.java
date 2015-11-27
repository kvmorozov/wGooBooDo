package ru.simpleGBD.App.Logic.Proxy;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by km on 23.11.2015.
 */
public class WebProxyListProvider extends AbstractProxyPistProvider {


    private static final String PROXY_LIST_URL = "http://webanetlabs.net/freeproxy/proxylist_at_24.11.2015.txt";

    public static final IProxyListProvider INSTANCE = new WebProxyListProvider();

    WebProxyListProvider() {
        buildList();
    }

    private void buildList() {
        try {
            Document doc = Jsoup
                    .connect(PROXY_LIST_URL)
                    .get();

            List<String> proxyItems = Arrays.asList(((TextNode) doc.child(0).child(1).childNode(0)).getWholeText().split("\\r\\n"));
            buildFromList(proxyItems);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
