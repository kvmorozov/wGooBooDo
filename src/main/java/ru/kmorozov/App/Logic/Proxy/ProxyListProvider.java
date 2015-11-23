package ru.kmorozov.App.Logic.Proxy;

import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import ru.kmorozov.App.Logic.ExecutionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by km on 23.11.2015.
 */
public class ProxyListProvider {

    private static Logger logger = Logger.getLogger(ProxyListProvider.class.getName());
    private static final String PROXY_LIST_URL = "http://ab57.ru/downloads/proxylist.txt";

    public static final ProxyListProvider INSTANCE = new ProxyListProvider();

    private List<HttpHost> proxyList;

    private ProxyListProvider() {
        buildList();
    }

    private void buildList() {
        try {
            Document doc = Jsoup
                    .connect(PROXY_LIST_URL)
                    .get();

            List<String> proxyItems = Arrays.asList(((TextNode) doc.child(0).child(1).childNode(0)).getWholeText().split("\\r\\n"));
            proxyList = new ArrayList<HttpHost>(proxyItems.size());
            for(String proxyItem : proxyItems) {
               String[] proxyItemArr = proxyItem.split(":");
                HttpHost host = new HttpHost(proxyItemArr[0], Integer.valueOf(proxyItemArr[1]));
                try {
                    if (host.getAddress().isReachable(100)) {
                        proxyList.add(host);
                        logger.info(String.format("Proxy %s added.", host.toHostString()));
                    }
                }
                catch(Exception ex) {

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<HttpHost> getProxyList() {
        return proxyList;
    }
}
