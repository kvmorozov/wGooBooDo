package ru.kmorozov.gbd.core.logic.Proxy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import ru.kmorozov.gbd.core.utils.HttpConnections;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by km on 23.11.2015.
 */
public class WebProxyListProvider extends AbstractProxyListProvider {

    private static final String PROXY_LIST_URL = "http://www.sslproxies.org/";

    WebProxyListProvider() {
        buildList();
    }

    private void buildList() {
        try {
            Document doc = Jsoup
                    .connect(PROXY_LIST_URL)
                    .userAgent(HttpConnections.USER_AGENT)
                    .get();

            this.proxyItems = doc.getElementById("proxylisttable").getElementsByTag("tbody").get(0).getElementsByTag("tr")
                    .stream().map(this::extractProxyData).filter(this::notBlacklisted).limit(20).collect(Collectors.toList());
            this.proxyItems.addAll(ProxyBlacklistHolder.BLACKLIST.getWhiteList());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractProxyData(Element element) {
        return ((TextNode) element.childNode(0).childNode(0)).getWholeText() + ":" + ((TextNode) element.childNode(1).childNode(0)).getWholeText();
    }

    @Override
    public void updateProxyList() {

    }
}